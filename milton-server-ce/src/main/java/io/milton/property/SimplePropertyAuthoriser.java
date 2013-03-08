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
