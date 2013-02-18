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

import io.milton.annotations.Get;
import io.milton.common.ModelAndView;
import io.milton.common.StreamUtils;
import io.milton.http.Range;
import io.milton.http.Request.Method;
import io.milton.http.template.TemplateProcessor;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class GetAnnotationHandler extends AbstractAnnotationHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GetAnnotationHandler.class);
	
	public GetAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, Get.class, Method.GET);
	}
	
	public void execute(AnnoResource resource, Object source, OutputStream out, Range range, Map<String, String> params, String contentType) {		
		ControllerMethod cm = getBestMethod(source.getClass(), contentType, params);
		if (cm == null) {
			throw new RuntimeException("Method not found: " + getClass() + " - " + source.getClass());
		}
		log.trace("execute GET method: " + cm.method.getName());
		try {
			Object[] args = outer.buildInvokeArgs(source, cm.method, range, params, contentType, out);
			Object result = cm.method.invoke(cm.controller, args);
			if (result != null) {
				log.trace("method returned a value, so write it to output");
				if (result instanceof String) {
					ModelAndView modelAndView = new ModelAndView("resource", source, result.toString());
					processTemplate(modelAndView, resource, out);
				} else if (result instanceof InputStream) {
					InputStream contentIn = (InputStream) result;
					if (range != null) {
						StreamUtils.readTo(contentIn, out, true, false, range.getStart(), range.getFinish());
					} else {
						try {
							IOUtils.copy(contentIn, out);
						} finally {
							IOUtils.closeQuietly(contentIn);
						}
					}
				} else if (result instanceof byte[]) {
					byte[] bytes = (byte[]) result;
					out.write(bytes);
				} else if (result instanceof ModelAndView) {
					processTemplate((ModelAndView) result, resource, out);
				} else {
					throw new RuntimeException("Unsupported return type from method: " + cm.method.getName() + " in " + cm.controller.getClass() + " should return String or byte[]");
				}
			}
			out.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void processTemplate(ModelAndView modelAndView,AnnoResource resource, OutputStream out) {
		TemplateProcessor templateProc = outer.getViewResolver().resolveView(modelAndView.getView());
		modelAndView.getModel().put("page", resource);
		templateProc.execute(modelAndView.getModel(), out);
	}
}
