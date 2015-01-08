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

import io.milton.annotations.Get;
import io.milton.common.JsonResult;
import io.milton.common.ModelAndView;
import io.milton.common.StreamUtils;
import io.milton.http.HttpManager;
import io.milton.http.Range;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.http.template.TemplateProcessor;
import io.milton.principal.DiscretePrincipal;
import java.io.IOException;

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

	public void execute(AnnoResource resource, OutputStream out, Range range, Map<String, String> params, String contentType)  throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
		Object source = resource.getSource();
		ControllerMethod cm = getBestMethod(source.getClass(), contentType, params, null);
		if (cm == null) {
			throw new RuntimeException("Method not found: " + getClass() + " - " + source.getClass());
		}
		log.trace("execute GET method: " + cm.method.getName());
		try {
			//Object[] args = annoResourceFactory.buildInvokeArgs(resource, cm.method, range, params, contentType, out);
			//Object result = cm.method.invoke(cm.controller, args);
			Object result = invoke(cm, resource, range, params, contentType, out);
			if (result != null) {
				log.trace("method returned a value, so write it to output");
				if (result instanceof String) {
					ModelAndView modelAndView = new ModelAndView("resource", source, result.toString());
					processTemplate(modelAndView, resource, out);
				} else if (result instanceof JsonResult) {
					JsonResult jsonr = (JsonResult) result;
					JsonWriter jsonWriter = new JsonWriter();
					jsonWriter.write(jsonr, out);

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
		} catch(IOException e) {
			throw e;			
		} catch(NotAuthorizedException e) {
			throw e;			
		} catch(BadRequestException e) {
			throw e;			
		} catch(NotFoundException e) {
			throw e;						
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void processTemplate(ModelAndView modelAndView, AnnoResource resource, OutputStream out) {
		TemplateProcessor templateProc = annoResourceFactory.getViewResolver().resolveView(modelAndView.getView());
		modelAndView.getModel().put("page", resource);
		if (HttpManager.request().getAuthorization() != null) {
			Object principal = HttpManager.request().getAuthorization().getTag();
			if (principal instanceof DiscretePrincipal) {
				modelAndView.getModel().put("principal", principal);
			}
		}

		templateProc.execute(modelAndView.getModel(), out);
	}
}
