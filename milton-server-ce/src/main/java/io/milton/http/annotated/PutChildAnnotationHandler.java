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

import io.milton.annotations.PutChild;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;

import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class PutChildAnnotationHandler extends AbstractAnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(PutChildAnnotationHandler.class);

	public PutChildAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, PutChild.class, Method.PUT);
	}

	public Object execute(AnnoResource res, String newName, InputStream inputStream, Long length, String contentType) throws ConflictException, NotAuthorizedException, BadRequestException {
		log.trace("execute PUT method");
		Object source = res.getSource();
		ControllerMethod cm = getBestMethod(source.getClass());
		if (cm == null) {
			if (controllerMethods.isEmpty()) {
				log.info("Method not found for source: {}. No methods registered for {}", source.getClass().getSimpleName(), PutChild.class.getSimpleName());
			} else {
				log.info("Method not found for source {}. Listing methods registered for {}: {}", new Object[]{source.getClass().getSimpleName(), PutChild.class.getSimpleName(), StringUtils.join(controllerMethods, ",")});
			}
			throw new RuntimeException("Method not found: " + getClass() + " - " + source.getClass());
		}
		try {
			//Object[] args = outer.buildInvokeArgs(source, cm.method, newName, inputStream, length, contentType);
			//return cm.method.invoke(cm.controller, args); 
			return invoke(cm, res, newName, inputStream, length, contentType);
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

	public void replace(AnnoFileResource fileRes, InputStream inputStream, Long length) throws ConflictException, NotAuthorizedException, BadRequestException {
		log.trace("execute PUT (replace) method");
		Object source = fileRes.getSource();
		ControllerMethod cm = getBestMethod(source.getClass());
		if (cm == null) {
			// ok, cant replace. Maybe we can delete and PUT?
			String name = fileRes.getName();
			annoResourceFactory.deleteAnnotationHandler.execute(fileRes);
			execute(fileRes.getParent(), name, inputStream, length, null);

		} else {
			try {
				invoke(cm, fileRes, inputStream, length, fileRes);
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
}
