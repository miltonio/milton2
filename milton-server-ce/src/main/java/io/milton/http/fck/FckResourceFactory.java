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
package io.milton.http.fck;

import io.milton.common.Path;
import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FckResourceFactory implements ResourceFactory {

	private static final Logger log = LoggerFactory.getLogger(FckResourceFactory.class);
	private final ResourceFactory wrappedFactory;

	public FckResourceFactory(ResourceFactory wrappedFactory) {
		this.wrappedFactory = wrappedFactory;
	}

	@Override
	public Resource getResource(String host, String url) throws NotAuthorizedException, BadRequestException {
		Path path = Path.path(url);
		if (FckFileManagerResource.URL.equals(path)) {
			CollectionResource h = getParent(host, path.getParent());
			if (h != null) {
				FckFileManagerResource fck = new FckFileManagerResource(h);
				return fck;
			}
		} else if (FckQuickUploaderResource.URL.equals(path)) {
			CollectionResource h = getParent(host, path.getParent());
			if (h != null) {
				FckQuickUploaderResource fck = new FckQuickUploaderResource(h);
				return fck;
			}
		}
		return wrappedFactory.getResource(host, url);
	}

	private CollectionResource getParent(String host, Path path) throws NotAuthorizedException, BadRequestException {
		Resource r = wrappedFactory.getResource(host, path.toString());
		if (r instanceof CollectionResource) {
			return (CollectionResource) r;
		} else {
			log.warn("Could not locate a CollectionResource at: http://" + host + "/" + path);
			return null;
		}
	}
}
