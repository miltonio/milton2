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
package io.milton.http.annotated;

import io.milton.annotations.AddressBooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class AddressBooksAnnotationHandler extends AbstractAnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(AddressBooksAnnotationHandler.class);

	public AddressBooksAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, AddressBooks.class);
	}

	/**
	 * Check if the source object is a source parameter for methods with the 
	 * @AddressBooks annotation
	 * 
	 * @param source
	 * @return 
	 */
	public boolean hasAddressBooks(Object source) {
		for( ControllerMethod cm : getMethods(source.getClass())) {
			AddressBooks c = cm.method.getAnnotation(AddressBooks.class);
			if( c != null ) {
				return true;
			}
		}
		log.warn("No address books found from annotation handler for source: " + source.getClass());
		return false;
	}

}
