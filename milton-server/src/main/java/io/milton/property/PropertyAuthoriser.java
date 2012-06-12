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
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author brad
 */
public interface PropertyAuthoriser {

    public enum PropertyPermission {
        READ,
        WRITE
    }

    /*
     * Check if the current user has permission to write to the given fields
     *
     * Returns null or an empty set to indicate that the request should be allowed,
     * or a set of fields which the current user does not have access to if
     * there has been a violation
     * 
     */
    Set<CheckResult> checkPermissions(Request request, Method method, PropertyPermission perm, Set<QName> fields, Resource resource);



    /**
     * Describes a permission violation.
     */
    public class CheckResult {
        private final QName field;
        private final Status status;
        private final String description;
        private final Resource resource;

        public CheckResult( QName field, Status status, String description, Resource resource ) {
            this.field = field;
            this.status = status;
            this.description = description;
            this.resource = resource;
        }

        public String getDescription() {
            return description;
        }

        public QName getField() {
            return field;
        }

        public Status getStatus() {
            return status;
        }

        public Resource getResource() {
            return resource;
        }
    }
}
