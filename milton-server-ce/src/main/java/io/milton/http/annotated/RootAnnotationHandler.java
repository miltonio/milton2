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

import io.milton.annotations.Root;
import io.milton.http.Request.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class RootAnnotationHandler implements AnnotationHandler {
	private final List<ControllerMethod> controllerMethods = new ArrayList<ControllerMethod>();
	private final AnnotationResourceFactory outer;

	public RootAnnotationHandler(final AnnotationResourceFactory outer) {
		this.outer = outer;
	}

	public Object execute(String host) {
		for (ControllerMethod cm : controllerMethods) {
			try {
				Object root;
				if (cm.method.getParameterTypes().length == 0) {
					root = cm.method.invoke(cm.controller);
				} else {
					root = cm.method.invoke(cm.controller, host); // TODO: other args like request, response, etc
					// TODO: other args like request, response, etc
					// TODO: other args like request, response, etc
				}
				if (root != null) {
					return root;
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	@Override
	public void parseController(Object controller) {
		for (java.lang.reflect.Method m : controller.getClass().getMethods()) {
			Root a = m.getAnnotation(Root.class);
			if (a != null) {
				ControllerMethod cm = new ControllerMethod(controller, m, null, null);
				controllerMethods.add(cm);
			}
		}
	}

	@Override
	public Method[] getSupportedMethods() {
		return null;
	}

	@Override
	public boolean isCompatible(Object source) {
		return false;
	}

	@Override
	public List<ControllerMethod> getControllerMethods() {
		return controllerMethods;
	}
    
	@Override
	public Class getAnnoClass() {
		return Root.class;
	}	
	
}
