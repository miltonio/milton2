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

import io.milton.annotations.ChildrenOf;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author brad
 */
public class ChildrenOfAnnotationHandler extends AbstractAnnotationHandler {

	public ChildrenOfAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, ChildrenOf.class, Method.PROPFIND);
	}

	public Set<AnnoResource> execute(AnnoCollectionResource parent, boolean isChildLookup) throws NotAuthorizedException, BadRequestException, NotFoundException, Exception {
		Set<AnnoResource> result = new HashSet<AnnoResource>();
		List<ControllerMethod> candidateMethods = getMethods(parent.source.getClass());
		// Find any override methods
		Set<Class> overrideSourceTypes = new HashSet<Class>();
		for (ControllerMethod cm : candidateMethods) {
			ChildrenOf anno = (ChildrenOf) cm.anno;
			if (anno.override()) {
				overrideSourceTypes.add(cm.sourceType);
			}
		}
		// If we have override methods, then remove any methods targeting base classes of the override source types
		if (overrideSourceTypes.size() > 0) {
			Iterator<ControllerMethod> it = candidateMethods.iterator();
			while (it.hasNext()) {
				Class sourceType = it.next().sourceType;
				for (Class overrideClass : overrideSourceTypes) {
					if (overrideClass != sourceType && sourceType.isAssignableFrom(overrideClass)) {
						it.remove();
						break;
					}
				}
			}
		}

		for (ControllerMethod cm : candidateMethods) {
			try {
				if (lookupPermitted(isChildLookup, cm)) {
					Object o = invoke(cm, parent);
					annoResourceFactory.createAndAppend(result, o, parent, cm);
				}
			} catch (NotAuthorizedException e) {
				throw e;
			} catch (BadRequestException e) {
				throw e;
			} catch (NotFoundException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		return result;
	}

	private boolean lookupPermitted(boolean childLookup, ControllerMethod cm) {
		ChildrenOf anno = (ChildrenOf) cm.anno;
		if (childLookup) {
			return anno.allowChildLookups();
		}
		return true;
	}
}
