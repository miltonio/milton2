/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
