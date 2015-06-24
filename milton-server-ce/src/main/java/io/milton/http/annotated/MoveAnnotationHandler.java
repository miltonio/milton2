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

import io.milton.annotations.Move;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class MoveAnnotationHandler extends AbstractAnnotationHandler {
	
	private static final Logger log = LoggerFactory.getLogger(MoveAnnotationHandler.class);
	
	public MoveAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, Move.class, Method.MOVE);
	}

	void execute(AnnoResource res, CollectionResource rDest, String newName)  throws ConflictException, NotAuthorizedException, BadRequestException{
		log.trace("execute MOVE method");
		Object source = res.getSource();
		ControllerMethod cm = getBestMethod(source.getClass());
		if (cm == null) {
			throw new RuntimeException("Method not found: " + getClass() + " - " + source.getClass());
		}
		try {
			Object destObject = null;
			if (rDest instanceof AnnoResource) {
				AnnoResource arDest = (AnnoResource) rDest;
				destObject = arDest.getSource();
			}
			Object[] args = annoResourceFactory.buildInvokeArgs(res, cm.method, newName, rDest, destObject);
			cm.method.invoke(cm.controller, args);
		} catch (NotAuthorizedException e) {
			throw e;
		} catch (BadRequestException e) {
			throw e;
		} catch (ConflictException e) {
			throw e;				
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    
}
