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

package io.milton.http.json;

import io.milton.http.FileItem;
import io.milton.http.Range;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.resource.GetableResource;
import io.milton.resource.PostableResource;
import io.milton.resource.Resource;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * This just allows the determination of the per method handling class to be
 * figured out during POST or GET processing, rather then requiring that
 * determination to be made solely on the basis of the url
 *
 * @author brad
 */
public class PostJsonResource extends JsonResource implements PostableResource {
	private final JsonResourceFactory jsonResourceFactory;
	private final String methodParamName;
	private final String host;
	private final String href;

	private Resource res; // this is the method handling instance

	
	public PostJsonResource(String host, String href, Resource wrappedResource, String methodParamName, JsonResourceFactory jsonResourceFactory) {
		super(wrappedResource, wrappedResource.getName(), null);
		this.methodParamName = methodParamName;
		this.jsonResourceFactory = jsonResourceFactory;
		this.host = host;
		this.href = href;
	}

	@Override
	public Method applicableMethod() {
		return Method.POST;
	}

	@Override
	public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws BadRequestException, NotAuthorizedException, ConflictException {
		String method = parameters.get(methodParamName);
		
		// what about allowing devs to invoke methods on their resources? do we
		// allow any method value, if it matches a method name?
		
		res = jsonResourceFactory.wrapResource(host, this, method, href);
		if( res instanceof PostableResource) {
			PostableResource pr = (PostableResource) res;
			return pr.processForm(parameters, files);
		} else {
			return null;
		}
	}

	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
		if( res == null) {
			String method = params.get(methodParamName);
			res = jsonResourceFactory.wrapResource(host, this, method, href);
		}
		if( res instanceof GetableResource) {
			GetableResource gr = (GetableResource) res;
			gr.sendContent(out, range, params, contentType);
		}
	}
	
}
