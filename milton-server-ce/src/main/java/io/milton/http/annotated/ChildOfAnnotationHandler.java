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

import io.milton.annotations.ChildOf;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class ChildOfAnnotationHandler extends AbstractAnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(ChildOfAnnotationHandler.class);
	public static final String NOT_ATTEMPTED = "NotAttempted";

	public ChildOfAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, ChildOf.class);
	}

	/**
	 * Will return one of: - ChildOfAnnotationHandler.NOT_ATTEMPTED if no
	 * appropriate method was found - null, if a method was available but no
	 * resource was found - or, the child object with the given name wrapped in
	 * an AnnoResource
	 *
	 * @param parent
	 * @param childName
	 * @return - a tri-value indicating the object which was found, no object
	 * was found, or search was not attempted
	 * @throws io.milton.http.exceptions.NotAuthorizedException
	 * @throws io.milton.http.exceptions.BadRequestException
	 * @throws io.milton.http.exceptions.NotFoundException
	 */
	public Object execute(AnnoCollectionResource parent, String childName) throws NotAuthorizedException, BadRequestException, NotFoundException {
		Object source = parent.getSource();
		try {
			List<ControllerMethod> availMethods = getMethods(source.getClass());
			if (availMethods.isEmpty()) {
				return NOT_ATTEMPTED;
			}
			
			for (ControllerMethod cm : availMethods) {
				if (matchesSuffix(cm, childName)) {
					Object childObject = invoke(cm, parent, childName);
					if (childObject == null) {
						// ignore
					} else {
						AnnoResource r = annoResourceFactory.instantiate(childObject, parent, cm.method);
						r.setNameOverride(childName);
						return r;
					}
				}
			}
		
		} catch (NotAuthorizedException e) {
			throw e;
		} catch(BadRequestException e) {
			throw e;
		} catch(NotFoundException e) {
			throw e;
		} catch(Exception e ) {
			throw new RuntimeException(e);
		} 
		return null;
	}

	private boolean matchesSuffix(ControllerMethod cm, String childName) {
		ChildOf a = (ChildOf) cm.anno;
		if (!a.pathSuffix().isEmpty()) {
			return childName.endsWith(a.pathSuffix());
		} else {
			return true;
		}
	}
}
