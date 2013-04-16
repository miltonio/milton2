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

import io.milton.annotations.Post;
import io.milton.common.JsonResult;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Map;
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
	 * @param out
	 * @param range
	 * @param params
	 * @param contentType
	 * @return
	 */
	public Object execute(AnnoResource resource, Request request, Map<String, String> params) {
		Object source = resource.getSource();
		ControllerMethod cm = getBestMethod(source.getClass(), null, params, null);
		if (cm == null) {
			throw new RuntimeException("Method not found: " + getClass() + " - " + source.getClass());
		}
		log.trace("execute POST method: " + cm.method.getName());
		Post a = cm.method.getAnnotation(Post.class);
		try {
			if (a.bindData()) {
				Locale locale = request.getLocale();
				if (locale == null) {
					locale = Locale.getDefault();
				}
				DataBinder dataBinder = new DataBinder(locale);
				dataBinder.populate(source, params);
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
