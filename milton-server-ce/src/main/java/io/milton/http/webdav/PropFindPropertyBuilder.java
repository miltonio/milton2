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

package io.milton.http.webdav;

import io.milton.resource.PropFindableResource;
import io.milton.resource.Resource;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.values.ValueAndType;
import java.net.URISyntaxException;
import java.util.*;
import javax.xml.namespace.QName;

/**
 * This class performs the main part of PROPFIND processing, which is given
 * a field request (either named fields or an allprop request) and a target
 * resource, iterate over that resource and its children (depending on the
 * depth header) and list a list of PropFindResponse objects.
 *
 * These PropFindResponse objects contain typed values for all of the known
 * fields, and a set of unknown fields. These will be used to build the xml
 * which is ultimately sent back to the client.
 *
 * This class uses a list of PropertySource's, where each PropertySource represents
 * some mechanism to read properties from a resource.
 *
 * @author brad
 */
public interface PropFindPropertyBuilder {


	/**
	 * Construct a list of PropFindResponse for the given resource, using
	 * the PropertySource's injected into this class.
	 *
	 *
	 * @param pfr - the resource to interrogate
	 * @param depth - the depth header. 0 means only look at the given resource. 1 is to include children
	 * @param parseResult - contains the list of fields, or a true boolean indicating all properties
	 * @param url - the URL of the given resource - MUST be correctly encoded
	 * @return
	 */
	List<PropFindResponse> buildProperties(PropFindableResource pfr, int depth, PropertiesRequest parseResult, String url) throws URISyntaxException, NotAuthorizedException, BadRequestException;

	ValueAndType getProperty(QName field, Resource resource) throws NotAuthorizedException, BadRequestException;

	void processResource(List<PropFindResponse> responses, PropFindableResource resource, PropertiesRequest parseResult, String href, int requestedDepth, int currentDepth, String collectionHref) throws NotAuthorizedException, BadRequestException;

	Set<QName> findAllProps(PropFindableResource resource) throws NotAuthorizedException, BadRequestException;

}
