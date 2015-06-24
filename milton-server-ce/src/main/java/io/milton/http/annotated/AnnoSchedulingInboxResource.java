/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.http.annotated;

import org.slf4j.LoggerFactory;

/**
 * Placeholder object to represent a node in an annotations hierarchy acting as a
 * collection
 *
 * A source object (ie your POJO) is considered a collection if it can have
 * children , ie if there exists at least one @ChildOf or @ChildrenOf method
 * which has that object as its source type. Note this is keyed on the class.
 *
 * This class includes methods suitable for use in page templating logic for
 * navigating through the hierarchy.
 *
 * @author brad
 */
public class AnnoSchedulingInboxResource extends AnnoCollectionResource {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(AnnoSchedulingInboxResource.class);


	public AnnoSchedulingInboxResource(final AnnotationResourceFactory outer, Object source, AnnoPrincipalResource parent) {
		super(outer, source, parent);
	}



	
}
