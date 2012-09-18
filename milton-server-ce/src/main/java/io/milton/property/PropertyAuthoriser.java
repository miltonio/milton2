/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
