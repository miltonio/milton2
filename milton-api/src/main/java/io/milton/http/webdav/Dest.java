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

package io.milton.http.webdav;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author brad
 * 
 * Represents the destination of a MOVE or COPY method
 * 
 * The target url is parsed into the 3 components, host, parent url and name
 * to make it easier to differentiate between the name and folder of the destination
 */
public class Dest {
    
    private static final Logger log = LoggerFactory.getLogger(Dest.class);
    
    public final String host;
    
    /**
     * path of the parent folder
     */
    public final String url;
    
    /**
     * the name portion of the destination
     */
    public final String name;
    
    
    
    public Dest(String sourceHost, String sDest ) {
        log.debug("sDest: " + sDest);
        String sUrl;
        if( sDest.endsWith("/") ) sDest = sDest.substring(0,sDest.length()-1);
        if( sDest.contains("http://") ) {
            String s = sDest.replace("http://","");
            int pos = s.indexOf(":");
            if( pos > 0 ) {
                host = s.substring(0,pos);
                pos = s.indexOf("/");
            } else {
                pos = s.indexOf("/");
                host = s.substring(0,pos);
            }
            sUrl = s.substring(pos);
        } else {
            host = sourceHost;
            sUrl = sDest;
        }
        int pos = sUrl.lastIndexOf("/");
        if( pos <= 0 ) {
            url = "/";
        } else {
            url = sUrl.substring(0,pos);
        }
        name = sUrl.substring(pos+1);
    }

	@Override
	public String toString() {
		return host + ":" + url + "/" + name;
	}
	
	
}