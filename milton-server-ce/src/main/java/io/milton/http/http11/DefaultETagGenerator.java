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

package io.milton.http.http11;

import io.milton.resource.Resource;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates the ETag as follows:
 *
 * - if the resource has a null unique id, returns null
 * - if the resource has a modified date it's hashcode is appended to the unique id
 * - the result is returned
 *
 * @author brad
 */
public class DefaultETagGenerator implements ETagGenerator {

    private static final Logger log = LoggerFactory.getLogger( DefaultETagGenerator.class );

	public DefaultETagGenerator() {
	}
	
	
	@Override
    public String generateEtag( Resource r ) {
        log.trace( "generateEtag" );
        String s = r.getUniqueId();
        if( s == null ) {
            log.trace("no uniqueId, so no etag");
            return null;
        } else {
            Date dt = r.getModifiedDate();
            if( dt != null ) {
                log.trace("combine uniqueId with modDate to make etag");
                s = s + "_" + dt.hashCode();
            } else {
                log.trace("no modDate, so etag is just unique id");
            }
            return "\"" + s + "\"";
        }
    }
}
