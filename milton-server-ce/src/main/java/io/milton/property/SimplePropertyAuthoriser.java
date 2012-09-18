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

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.resource.Resource;
import io.milton.http.Response.Status;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * Very basic implementation for development and prototyping
 *
 * Allows all logged in access
 *
 * @author brad
 */
public class SimplePropertyAuthoriser implements PropertyAuthoriser {

	@Override
    public Set<CheckResult> checkPermissions( Request request, Method method, PropertyPermission perm, Set<QName> fields, Resource resource ) {
        Auth auth = request.getAuthorization();
        if( auth != null && auth.getTag() != null ) {
            return null;
        } else {
            Set<CheckResult> s = new HashSet<CheckResult>();
            for( QName qn : fields ) {
                s.add(new CheckResult( qn, Status.SC_UNAUTHORIZED, "Not logged in", resource));
            }
            return s;
        }
    }

}
