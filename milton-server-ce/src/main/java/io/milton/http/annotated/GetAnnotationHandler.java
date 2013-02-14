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
import io.milton.http.Range;
import io.milton.http.Request.Method;
import java.io.OutputStream;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class GetAnnotationHandler extends AbstractAnnotationHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GetAnnotationHandler.class);
	
	private final AnnotationResourceFactory outer;

	public GetAnnotationHandler(final AnnotationResourceFactory outer) {
		super(Get.class, Method.GET);
		this.outer = outer;
	}

	public void execute(Object source, OutputStream out, Range range, Map<String, String> params, String contentType) {
		log.trace("execute GET method");
		ControllerMethod cm = getMethod(source.getClass());
		if (cm == null) {
			throw new RuntimeException("Method not found: " + getClass() + " - " + source.getClass());
		}
		try {
			Object[] args = outer.buildInvokeArgs(source, cm.method, range, params, contentType, out);
			Object result = cm.method.invoke(cm.controller, args); // TODO: other args like request, response, etc
			// TODO: other args like request, response, etc
			// TODO: other args like request, response, etc
			if (result != null) {
				log.trace("method returned a value, so write it to output");
				byte[] bytes;
				if (result instanceof String) {
					// todo: templating
					throw new RuntimeException("String return type not yet supported. Use byte[] instead");
				} else if (result instanceof byte[]) {
					bytes = (byte[]) result;
				} else {
					throw new RuntimeException("Unsupported return type from method: " + cm.method.getName() + " in " + cm.controller.getClass() + " should return String or byte[]");
				}
				out.write(bytes);
			}
			out.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    
}
