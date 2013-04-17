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

import io.milton.annotations.Root;
import io.milton.http.Request.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class RootAnnotationHandler implements AnnotationHandler {
	private List<ControllerMethod> controllerMethods = new ArrayList<ControllerMethod>();
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
