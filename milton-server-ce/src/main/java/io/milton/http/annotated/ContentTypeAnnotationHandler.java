/*
 * Copyright 2013 McEvoy Software Ltd.
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

import io.milton.annotations.ContentType;
import io.milton.common.ContentTypeUtils;

/**
 *
 * @author brad
 */
public class ContentTypeAnnotationHandler extends AbstractAnnotationHandler {

	protected final String[] propertyNames;

	public ContentTypeAnnotationHandler(AnnotationResourceFactory outer, String... propNames) {
		super(outer, ContentType.class);
		this.propertyNames = propNames;
	}

	public String get(String accepts, AnnoResource res) {
		Object source = res.getSource();
		try {			
			ControllerMethod cm = getBestMethod(source.getClass(), null, null, Object.class);
			if (cm != null) {
				String val = (String) invoke(cm, res, accepts);
				return val;
			} else {
				// look for an annotation on the source itself
				java.lang.reflect.Method m = annoResourceFactory.findMethodForAnno(source.getClass(), annoClass);
				if (m != null) {
					String val = (String) m.invoke(source, accepts);
					return val;
				}
				for (String propName : propertyNames) {
					Object s = attemptToReadProperty(source, propName);
					if (s != null) {
						return (String) s;
					}
				}
				return ContentTypeUtils.findAcceptableContentTypeForName(res.getName(), accepts);
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception executing " + annoClass + " - " + source.getClass(), e);
		}
	}
}
