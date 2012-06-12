/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.milton.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class EventManagerImpl implements EventManager {

    private final static Logger log = LoggerFactory.getLogger( EventManagerImpl.class );
    private final Map<Class, List<EventListener>> listenersMap = new HashMap<Class, List<EventListener>>();

    @Override
    public void fireEvent( Event e ) {
        if( log.isTraceEnabled() ) {
            log.trace( "fireEvent: " + e.getClass().getCanonicalName() );
        }
        List<EventListener> list = listenersMap.get( e.getClass() );
        if( list == null ) return;
        for( EventListener l : Collections.unmodifiableCollection(list) ) {
            if( log.isTraceEnabled() ) {
                log.trace( "  firing on: " + l.getClass() );
            }
            l.onEvent( e );
        }
    }

    @Override
    public synchronized <T extends Event> void registerEventListener( EventListener l, Class<T> c ) {
        log.info( "registerEventListener: " + l.getClass().getCanonicalName() + " - " + c.getCanonicalName() );
        List<EventListener> list = listenersMap.get( c );
        if( list == null ) {
            list = new ArrayList<EventListener>();
            listenersMap.put( c, list );
        }
        list.add( l );
    }
}
