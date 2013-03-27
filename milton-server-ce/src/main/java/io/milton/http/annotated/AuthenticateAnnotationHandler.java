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

import io.milton.annotations.Authenticate;
import io.milton.http.http11.auth.DigestGenerator;
import io.milton.http.http11.auth.DigestResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class AuthenticateAnnotationHandler extends AbstractAnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(AuthenticateAnnotationHandler.class);
	public static final String NOT_ATTEMPTED = "NotAttempted";

	public AuthenticateAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, Authenticate.class);
	}

	public boolean canAuthenticate(Object source) {
		List<ControllerMethod> availMethods = getMethods(source.getClass());
		return !availMethods.isEmpty();
	}
	
	public Boolean authenticate(AnnoPrincipalResource userRes, String requestedPassword) {
		Object source = userRes.getSource();
		List<ControllerMethod> availMethods = getMethods(source.getClass());
		if (availMethods.isEmpty()) {
			return null;
		}
		try {
			for (ControllerMethod cm : availMethods) {
				// if it returns String then it returns a password. Otherwise is authenticate method
				if (cm.method.getReturnType().equals(String.class)) {
					String result = (String) invoke(cm, userRes, cm.method, userRes);
					if (result == null) {
						log.warn("Null password from: " + cm + " for user: " + userRes.getHref());
						return false;
					} else {
						return result.equals(requestedPassword);
					}
				} else if (cm.method.getReturnType().equals(Boolean.class)) {
					Object[] args = outer.buildInvokeArgs(userRes, cm.method, requestedPassword);
					Boolean result = (Boolean) cm.method.invoke(cm.controller, args);
					if (result != null) {
						return result;
					}
				} else {
					throw new RuntimeException("@Authenticate method does not return either String or Boolean: " + cm);
				}
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Boolean authenticate(AnnoPrincipalResource userRes, DigestResponse digestRequest) {
		Object source = userRes.getSource();
		List<ControllerMethod> availMethods = getMethods(source.getClass());
		if (availMethods.isEmpty()) {
			return null;
		}
		try {
			for (ControllerMethod cm : availMethods) {
				// if it returns String then it returns a password. Otherwise is authenticate method
				if (cm.method.getReturnType().equals(String.class)) {
					Object[] args = outer.buildInvokeArgs(userRes, cm.method, userRes);
					String result = (String) cm.method.invoke(cm.controller, args);
					if (result == null) {
						log.warn("Null password from: " + cm + " for user: " + userRes.getHref());
						return false;
					} else {
						DigestGenerator gen = new DigestGenerator();
						String actual = gen.generateDigest(digestRequest, result);
						if (actual.equals(digestRequest.getResponseDigest())) {
							return true;
						} else {
							log.warn("Password digest's dont match");
							return false;
						}
					}
				} else if (cm.method.getReturnType().equals(Boolean.class)) {
					Object[] args = outer.buildInvokeArgs(userRes, cm.method, digestRequest);
					Boolean result = (Boolean) cm.method.invoke(cm.controller, args);
					if (result != null) {
						return result;
					}
				} else {
					throw new RuntimeException("@Authenticate method does not return either String or Boolean: " + cm);
				}
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
