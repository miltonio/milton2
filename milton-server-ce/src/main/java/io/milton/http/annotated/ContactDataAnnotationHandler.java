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

import io.milton.annotations.ContactData;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author brad
 */
public class ContactDataAnnotationHandler extends AbstractAnnotationHandler {

	private final String[] PROP_NAMES = {"vcard", "contactData"};

	public ContactDataAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, ContactData.class);
	}

	public String execute(AnnoContactResource contactRes) {
		Object source = contactRes.getSource();
		try {
			Object value = null;
			ControllerMethod cm = getBestMethod(source.getClass());
			if (cm == null) {
				// look for an annotation on the source itself
				java.lang.reflect.Method m = annoResourceFactory.findMethodForAnno(source.getClass(), annoClass);
				if (m != null) {
					value = m.invoke(source, (Object) null);
				} else {
					for (String nameProp : PROP_NAMES) {
						if (PropertyUtils.isReadable(source, nameProp)) {
							Object oPropVal = PropertyUtils.getProperty(source, nameProp);
							value = oPropVal;
							break;
						}
					}
				}
			} else {
				value = invoke(cm, contactRes);
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
