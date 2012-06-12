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

package io.milton.property;

import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.resource.Resource;
import io.milton.http.Response.Status;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This default implementation is to allow property access based on the request.
 * 
 * Ie if a user has PROPPATCH access they will be permitted to patch all properties.
 * If they have PROPFIND access they will be permitted to read all properties
 *
 * @author brad
 */
public class DefaultPropertyAuthoriser implements PropertyAuthoriser {

	private static final Logger log = LoggerFactory.getLogger(DefaultPropertyAuthoriser.class);
	
	@Override
    public Set<CheckResult> checkPermissions( Request request, Method method, PropertyPermission perm, Set<QName> fields, Resource resource ) {
        if( resource.authorise( request, request.getMethod(), request.getAuthorization() ) ) {
			log.trace("checkPermissions: ok");
            return null;
        } else {
            // return all properties
			log.info("checkPermissions: property authorisation failed because user does not have permission for method: " + method.code);
            Set<CheckResult> set = new HashSet<CheckResult>();
            for( QName name : fields ) {
                set.add( new CheckResult( name, Status.SC_UNAUTHORIZED, "Not authorised", resource));
            }
            return set;
        }
    }
}
