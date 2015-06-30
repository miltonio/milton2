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

import io.milton.annotations.MakeCalendar;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import java.util.Map;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class MakeCalendarAnnotationHandler extends AbstractAnnotationHandler {
	
	private static final Logger log = LoggerFactory.getLogger(MakeCalendarAnnotationHandler.class);

	public MakeCalendarAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, MakeCalendar.class, Method.MKCALENDAR);
	}

	public Object execute(AnnoResource res, String newName, Map<QName, String> fieldsToSet)  throws NotAuthorizedException, ConflictException, BadRequestException {
		log.trace("execute MKCALENDAR method");
		Object source = res.getSource();
		ControllerMethod cm = getBestMethod(source.getClass());
		if (cm == null) {
			throw new RuntimeException("Method not found: " + getClass() + " - " + source.getClass());
		}
		try {
			Object[] args = annoResourceFactory.buildInvokeArgs(res, cm.method, newName, fieldsToSet);
			Object o = cm.method.invoke(cm.controller, args);
			if( o == null ) {
				throw new RuntimeException("Method returned null object or void: " + cm.controller.getClass() + "::" + cm.method.getName() + " - should return newly created object");
			}
			return o;
		} catch(NotAuthorizedException e) {
			throw e;			
		} catch(BadRequestException e) {
			throw e;			
		} catch(ConflictException e) {
			throw e;					
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    
}
