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

package io.milton.http.caldav.demo;

import io.milton.http.Range;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.ICalResource;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class TEvent extends TResource implements ICalResource {

    private static final Logger log = LoggerFactory.getLogger( TEvent.class );
    private String iCalData;

    public TEvent( TFolderResource parent, String name ) {
        super( parent, name );
    }

    @Override
    protected Object clone( TFolderResource newParent ) {
        TEvent e = new TEvent( (TCalendarResource) newParent, name );
        e.setiCalData( iCalData );
        return e;
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        out.write( iCalData.getBytes() );
    }

    @Override
    public String getContentType( String accepts ) {
        return "text/calendar";
    }

    @Override
    public String getICalData() {
        return iCalData;
    }

    public void setiCalData( String iCalData ) {
        this.iCalData = iCalData;
    }
}
