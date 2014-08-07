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
package io.milton.http.entity;

import io.milton.http.Response;
import io.milton.http.webdav.UserAgentHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The normal, trivial, implementation of EntityTransport which simply writes
 * immediately and directly to the Response output stream
 *
 * @author brad
 */
public class DefaultEntityTransport implements EntityTransport {

	private static final Logger log = LoggerFactory.getLogger(DefaultEntityTransport.class);
	
	private UserAgentHelper userAgentHelper;
	
	public DefaultEntityTransport(UserAgentHelper userAgentHelper) {
		this.userAgentHelper = userAgentHelper;
	}

	@Override
	public void sendResponseEntity(Response response) throws Exception {
		//log.info("send entity: " + response.getEntity());
		if (response.getEntity() != null) {
			response.getEntity().write(response, response.getOutputStream());
		} else {
			log.warn("No response entity to send!");
		}
	}

	@Override
	public void closeResponse(Response response) {
		response.close();
	}

	public UserAgentHelper getUserAgentHelper() {
		return userAgentHelper;
	}

	public void setUserAgentHelper(UserAgentHelper userAgentHelper) {
		this.userAgentHelper = userAgentHelper;
	}
	
}
