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

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.GetableResource;
import io.milton.resource.ReplaceableResource;
import java.io.InputStream;

/**
 *
 * @author brad
 */
public class AnnoFileResource extends AnnoResource implements GetableResource, ReplaceableResource {

	public AnnoFileResource(final AnnotationResourceFactory outer, Object source, AnnoCollectionResource parent) {
		super(outer, source, parent);
	}

	@Override
	public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
		annoFactory.putChildAnnotationHandler.replace(this, in, length);
	}

	@Override
	public boolean is(String type) {
		boolean b = super.is(type);
		if( b ) {
			return true;
		}
		String s = getContentType(null);
		return s != null && s.contains(type);
	}
	
	
}
