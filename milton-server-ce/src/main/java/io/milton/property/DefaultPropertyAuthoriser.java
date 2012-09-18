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

	public DefaultPropertyAuthoriser() {
	}
		
	
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
