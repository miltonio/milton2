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
        if ( r == null ) {
        	return null;
        }
        String s = r.getUniqueId();
        if( s == null ) {
			if( log.isTraceEnabled()) {
				log.trace("no uniqueId, so no etag for class: {}", r.getClass());
			}
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
