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

import io.milton.annotations.Post;
import io.milton.common.JsonResult;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class PostAnnotationHandler extends AbstractAnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(PostAnnotationHandler.class);

	public PostAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, Post.class, Method.POST);
	}

	/**
	 * Can return a String (meaning redirect to url), or a JsonResult which will
	 * be rendered to output in the sendContent phase, or null. If null is
	 * returned the sendContent phase will use normal GET processing
	 *
	 * @param resource
	 * @param request
	 * @param params
	 * @return
	 * @throws io.milton.http.exceptions.BadRequestException
	 * @throws io.milton.http.exceptions.NotAuthorizedException
	 * @throws io.milton.http.exceptions.ConflictException
	 */
	public Object execute(AnnoResource resource, Request request, Map<String, String> params) throws BadRequestException, NotAuthorizedException, ConflictException {
		Object source = resource.getSource();
		ControllerMethod cm = getBestMethod(source.getClass(), null, params, null);
		if (cm == null) {
			throw new RuntimeException("Method not found: " + getClass() + " - " + source.getClass());
		}
		log.trace("execute POST method: " + cm.method.getName());
		Post a = cm.method.getAnnotation(Post.class);
		try {
			if (a.bindData()) {
				TimeZone tz = null;
				if (a.timeZoneParam().length() > 0) {
					String sTimezone = DataBinder.getRawParam(params, a.timeZoneParam());					
					if (sTimezone != null) {
						tz = TimeZone.getTimeZone(sTimezone);
					}
				}
				DataBinder dataBinder = new DataBinder();
				dataBinder.populate(source, params, tz);
				resource.setNameOverride(null); // clear the name set by new object handling so created name will be returned
			}
		} catch (IllegalAccessException e) {
			log.warn("Exception running DataBinder:", e);
			return JsonResult.error(e.getMessage());
		} catch (InvocationTargetException e) {
			log.warn("Exception running DataBinder:", e);
			return JsonResult.error(e.getMessage());
		}

		try {
			Object[] args = annoResourceFactory.buildInvokeArgs(resource, cm.method, params);
			Object result = cm.method.invoke(cm.controller, args);
			return result;
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

	public ControllerMethod getPostMethod(AnnoResource resource, Request request, Map<String, String> params) {
		Object source = resource.getSource();
		ControllerMethod cm = getBestMethod(source.getClass(), null, params, null);
		return cm;
	}
}
