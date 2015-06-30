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

import io.milton.annotations.ICalData;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author brad
 */
public class ICalDataAnnotationHandler extends AbstractAnnotationHandler {

	private final String[] CTAG_PROP_NAMES = {"ical", "icalData"};

	public ICalDataAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, ICalData.class);
	}

	public String execute(AnnoEventResource eventRes) {
		Object source = eventRes.getSource();
		try {			
			Object value = null;
			ControllerMethod cm = getBestMethod(source.getClass());
			if (cm == null) {
				// look for an annotation on the source itself
				java.lang.reflect.Method m = annoResourceFactory.findMethodForAnno(source.getClass(), annoClass);
				if (m != null) {
					value = m.invoke(source, (Object) null);
				} else {
					for (String nameProp : CTAG_PROP_NAMES) {
						if (PropertyUtils.isReadable(source, nameProp)) {
							Object oPropVal = PropertyUtils.getProperty(source, nameProp);
							value = oPropVal;
							break;
						}
					}
				}
			} else {
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				value = invoke(cm, eventRes, bout);
				// These methods will often write to output stream, so must provide one as alternative to returning a value
				if( value == null ) { // probably means void return type, so use outputstream
					byte[] arr = bout.toByteArray();
					if( arr.length > 0 ) {
						value = arr;
					}
				}
			}
			if (value != null) {
				if( value instanceof String ) {
					return (String) value;
				} else if (value instanceof byte[]) {
					byte[] bytes = (byte[]) value;
					return new String(bytes, "UTF-8");
				} else if( value instanceof InputStream) {
					InputStream in = (InputStream) value;
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					IOUtils.copy(in, bout);
					return bout.toString("UTF-8");
				} else {			
					return value.toString();
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception executing " + getClass() + " - " + source.getClass(), e);
		}
	}
	

}
