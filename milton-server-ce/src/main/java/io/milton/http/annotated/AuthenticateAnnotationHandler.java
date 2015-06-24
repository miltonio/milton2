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

import io.milton.annotations.Authenticate;
import io.milton.http.http11.auth.DigestGenerator;
import io.milton.http.http11.auth.DigestResponse;
import java.lang.reflect.Method;
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
			if (!controllerMethods.isEmpty()) {
				if (log.isInfoEnabled()) {
					log.warn("No @Authenticate methods were found for user object: " + source + " located at: " + userRes.getHref());
				}
			}
			return null;
		}

		for (ControllerMethod cm : availMethods) {

			// if it returns String then it returns a password. Otherwise is authenticate method
			if (cm.method.getReturnType().equals(String.class)) {
				try {
					String result = (String) invoke(cm, userRes, cm.method, userRes);
					if (result == null) {
						log.warn("Null password from: " + cm + " for user: " + userRes.getHref());
						return false;
					} else {
						return result.equals(requestedPassword);
					}
				} catch (Exception ex) {
					throw new RuntimeException("Exception invoking @Authenticate method: " + cm.method, ex);
				}
			} else if (cm.method.getReturnType().equals(Boolean.class)) {
				if (hasParamType(cm.method, String.class)) { // Must have a string parameter for the password
					try {
						Object[] args = annoResourceFactory.buildInvokeArgs(userRes, cm.method, requestedPassword);
						Boolean result = (Boolean) cm.method.invoke(cm.controller, args);
						if (result != null) {
							return result;
						}
					} catch (Exception ex) {
						throw new RuntimeException("Exception invoking @Authenticate method: " + cm.method, ex);
					}
				}
			} else {
				throw new RuntimeException("@Authenticate method does not return either String or Boolean: " + cm);
			}

		}
		return null;

	}

	public Boolean authenticate(AnnoPrincipalResource userRes, DigestResponse digestRequest) {
		Object source = userRes.getSource();
		List<ControllerMethod> availMethods = getMethods(source.getClass());
		if (availMethods.isEmpty()) {
			return null;
		}
		try {
			for (ControllerMethod cm : availMethods) {
				if (cm.method.getReturnType().equals(String.class)) {
					try {
						String actualPassword = (String) invoke(cm, userRes, cm.method, userRes);
						if (actualPassword == null) {
							log.warn("Null password from: " + cm + " for user: " + userRes.getHref());
							return false;
						} else {
							DigestGenerator gen = new DigestGenerator();
							String expectedResp = gen.generateDigest(digestRequest, actualPassword);
							if (expectedResp.equals(digestRequest.getResponseDigest())) {
								return true;
							} else {
								log.info("Digest authentication failed, given digest response is not equal to expected");
								return false;
							}
						}
					} catch (Exception ex) {
						throw new RuntimeException("Exception invoking @Authenticate method: " + cm.method, ex);
					}
				} else if (hasParamType(cm.method, DigestResponse.class)) {
					// if it returns String then it returns a password. Otherwise is authenticate method
					if (cm.method.getReturnType().equals(String.class)) {
						Object[] args = annoResourceFactory.buildInvokeArgs(userRes, cm.method, userRes);
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
						Object[] args = annoResourceFactory.buildInvokeArgs(userRes, cm.method, digestRequest);
						Boolean result = (Boolean) cm.method.invoke(cm.controller, args);
						if (result != null) {
							return result;
						}
					} else {
						throw new RuntimeException("@Authenticate method does not return either String or Boolean: " + cm);
					}
				}
			}
			log.warn("Could not find any @Authentication methods compatible with Digest authentication");
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean hasParamType(Method method, Class type) {
		for (Class c : method.getParameterTypes()) {
			if (c.isAssignableFrom(type)) {
				return true;
			}
		}
		return false;
	}
}
