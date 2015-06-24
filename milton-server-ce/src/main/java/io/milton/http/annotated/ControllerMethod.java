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

import java.lang.annotation.Annotation;

/**
 *
 * @author brad
 */
public class ControllerMethod {

	final Object controller;
	final java.lang.reflect.Method method;
	final Class sourceType;
	final Annotation anno;

	public ControllerMethod(Object controller, java.lang.reflect.Method method, Class sourceType, final Annotation anno) {
		this.controller = controller;
		this.method = method;
		this.sourceType = sourceType;
		this.anno = anno;
	}

	@Override
	public String toString() {
		if (sourceType != null) {
			return controller.getClass() + "::" + method.getName() + " ( " + sourceType.getCanonicalName() + " )";
		} else {
			return controller.getClass() + "::" + method.getName();
		}
	}
}
