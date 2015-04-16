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

import io.milton.resource.CollectionResource;
import io.milton.resource.PropFindableResource;
import io.milton.resource.Resource;
import io.milton.http.Response.Status;
import io.milton.common.Utils;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.values.ValueAndType;
import io.milton.http.webdav.PropFindResponse.NameAndError;
import io.milton.property.PropertySource;
import io.milton.property.PropertySource.PropertyMetaData;
import io.milton.common.LogUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs the main part of PROPFIND processing, which is given a
 * field request (either named fields or an allprop request) and a target
 * resource, iterate over that resource and its children (depending on the depth
 * header) and list a list of PropFindResponse objects.
 *
 * These PropFindResponse objects contain typed values for all of the known
 * fields, and a set of unknown fields. These will be used to build the xml
 * which is ultimately sent back to the client.
 *
 * This class uses a list of PropertySource's, where each PropertySource
 * represents some mechanism to read properties from a resource.
 *
 * @author brad
 */
public class DefaultPropFindPropertyBuilder implements PropFindPropertyBuilder {

	private static final Logger log = LoggerFactory.getLogger(DefaultPropFindPropertyBuilder.class);
	private final List<PropertySource> propertySources;

	/**
	 *
	 * @param propertySources - the list of property sources used to read
	 * properties from resources
	 */
	public DefaultPropFindPropertyBuilder(List<PropertySource> propertySources) {
		this.propertySources = propertySources;
	}

	/**
	 * Construct a list of PropFindResponse for the given resource, using the
	 * PropertySource's injected into this class.
	 *
	 *
	 * @param pfr - the resource to interrogate
	 * @param depth - the depth header. 0 means only look at the given resource.
	 * 1 is to include children
	 * @param parseResult - contains the list of fields, or a true boolean
	 * indicating all properties
	 * @param url - the URL of the given resource - MUST be correctly encoded
	 * @return
	 */
	@Override
	public List<PropFindResponse> buildProperties(PropFindableResource pfr, int depth, PropertiesRequest parseResult, String url) throws URISyntaxException, NotAuthorizedException, BadRequestException {
		LogUtils.trace(log, "buildProperties: ", pfr.getClass(), "url:", url);
		url = fixUrlForWindows(url);
		List<PropFindResponse> propFindResponses = new ArrayList<PropFindResponse>();
		appendResponses(propFindResponses, pfr, depth, parseResult, url);
		return propFindResponses;
	}

	@Override
	public ValueAndType getProperty(QName field, Resource resource) throws NotAuthorizedException, BadRequestException {
		for (PropertySource source : propertySources) {
			PropertyMetaData meta = source.getPropertyMetaData(field, resource);
			if (meta != null && !meta.isUnknown()) {
				Object val = source.getProperty(field, resource);
				return new ValueAndType(val, meta.getValueType());
			}
		}
		LogUtils.trace(log, "getProperty: property not found", field, "resource", resource.getClass(), "property sources", propertySources);
		return null;
	}

	private void appendResponses(List<PropFindResponse> responses, PropFindableResource resource, int requestedDepth, PropertiesRequest parseResult, String encodedCollectionUrl) throws URISyntaxException, NotAuthorizedException, BadRequestException {
		String collectionHref = suffixSlash(resource, encodedCollectionUrl);
		URI parentUri = new URI(collectionHref);

		collectionHref = parentUri.toASCIIString();
		processResource(responses, resource, parseResult, collectionHref, requestedDepth, 0, collectionHref);

	}

	@Override
	public void processResource(List<PropFindResponse> responses, PropFindableResource resource, PropertiesRequest parseResult, String href, int requestedDepth, int currentDepth, String collectionHref) throws NotAuthorizedException, BadRequestException {
		final LinkedHashMap<QName, ValueAndType> knownProperties = new LinkedHashMap<QName, ValueAndType>();
		final ArrayList<NameAndError> unknownProperties = new ArrayList<NameAndError>();

		if (resource instanceof CollectionResource) {
			if (!href.endsWith("/")) {
				href = href + "/";
			}
		}
		Set<QName> requestedFields;
		if (parseResult.isAllProp()) {
			requestedFields = findAllProps(resource);
		} else {
			requestedFields = parseResult.getNames();
		}
		Iterator<QName> it = requestedFields.iterator();
		while (it.hasNext()) {
			QName field = it.next();
			LogUtils.trace(log, "processResource: find property:", field);
			if (field.getLocalPart().equals("href")) {
				knownProperties.put(field, new ValueAndType(href, String.class));
			} else {
				boolean found = false;
				for (PropertySource source : propertySources) {
					LogUtils.trace(log, "look for field", field, " in property source", source.getClass());
					PropertyMetaData meta = source.getPropertyMetaData(field, resource);
					if (meta != null && !meta.isUnknown()) {
						Object val;
						try {
							val = source.getProperty(field, resource);
							LogUtils.trace(log, "processResource: got value", val, "from source", source.getClass());
							if (val == null) {
								knownProperties.put(field, new ValueAndType(val, meta.getValueType())); // null, but we still need type information to write it so use meta
							} else {
								knownProperties.put(field, new ValueAndType(val, val.getClass())); // non-null, so use more robust class info
							}
						} catch (NotAuthorizedException ex) {
							unknownProperties.add(new NameAndError(field, "Not authorised"));
						}
						found = true;
						break;
					}
				}
				if (!found) {
					if (log.isDebugEnabled()) {
						log.debug("property not found in any property source: " + field.toString());
					}
					unknownProperties.add(new NameAndError(field, null));
				}

			}
		}
		if (log.isDebugEnabled()) {
			if (unknownProperties.size() > 0) {
				log.debug("some properties could not be resolved. Listing property sources:");
				for (PropertySource ps : propertySources) {
					log.debug(" - " + ps.getClass().getCanonicalName());
				}
			}
		}

		//Map<Status, List<NameAndError>> errorProperties = new HashMap<Status, List<NameAndError>>();
		Map<Status, List<NameAndError>> errorProperties = new EnumMap<Status, List<NameAndError>>(Status.class);
		errorProperties.put(Status.SC_NOT_FOUND, unknownProperties);
		PropFindResponse r = new PropFindResponse(href, knownProperties, errorProperties);
		responses.add(r);

		if (requestedDepth > currentDepth && resource instanceof CollectionResource) {
			CollectionResource col = (CollectionResource) resource;
			List<? extends Resource> list = col.getChildren();
			if (list != null) {
				list = new ArrayList<Resource>(list);
				for (Resource child : list) {
					if (child instanceof PropFindableResource) {
						String childName = child.getName();
						if (childName == null) {
							log.warn("null name for resource of type: " + child.getClass() + " in folder: " + href + " WILL NOT be returned in PROPFIND response!!");
						} else {
							String childHref = href + Utils.percentEncode(childName);
							// Note that the new collection href, is just the current href
							processResource(responses, (PropFindableResource) child, parseResult, childHref, requestedDepth, currentDepth + 1, href);
						}
					}
				}
			}
		}

	}

	private String suffixSlash(PropFindableResource resource, String s) {
		if (resource instanceof CollectionResource && !s.endsWith("/")) {
			s = s + "/";
		}
		return s;
	}

	@Override
	public Set<QName> findAllProps(PropFindableResource resource) throws NotAuthorizedException, BadRequestException {
		Set<QName> names = new LinkedHashSet<QName>();
		for (PropertySource source : this.propertySources) {
			List<QName> allprops = source.getAllPropertyNames(resource);
			if (allprops != null) {
				names.addAll(allprops);
			}
		}
		return names;
	}

	/**
	 * Requested URL *should* never contain an ampersand because its a reserved
	 * character. However windows 7 does send unencoded ampersands in requests,
	 * but expects them to be encoded in responses.
	 *
	 * @param url
	 * @return
	 */
	public static String fixUrlForWindows(String url) {
		//return url;
		return url.replace("&", "%26");
	}
}
