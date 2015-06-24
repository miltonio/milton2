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

import io.milton.annotations.CTag;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;
import java.util.Date;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class CTagAnnotationHandler extends AbstractAnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(CTagAnnotationHandler.class);

	public static String deriveCtag(CollectionResource col) throws NotAuthorizedException, BadRequestException {
		Date latest = col.getModifiedDate();
		for (Resource r : col.getChildren()) {
			Date d = r.getModifiedDate();
			if (d != null) {
				if (latest == null || d.after(latest)) {
					latest = d;
				}
			}
		}

		String ctag = null;
		if (latest != null) {
			ctag = "T" + latest.getTime();
		}
		return ctag;
	}
	private final String[] CTAG_PROP_NAMES = {"ctag"};

	public CTagAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, CTag.class);
	}

	public String execute(AnnoCollectionResource col) {
		Object source = col.getSource();
		try {
			Object rawId = null;
			ControllerMethod cm = getBestMethod(source.getClass());
			if (cm == null) {
				// look for an annotation on the source itself
				java.lang.reflect.Method m = annoResourceFactory.findMethodForAnno(source.getClass(), annoClass);
				if (m != null) {
					rawId = m.invoke(source);
					if (log.isDebugEnabled()) {
						log.debug("Got ctag from source object. ctag=" + rawId);
					}

				} else {
					for (String nameProp : CTAG_PROP_NAMES) {
						if (PropertyUtils.isReadable(source, nameProp)) {
							Object oPropVal = PropertyUtils.getProperty(source, nameProp);
							rawId = oPropVal;
							if (log.isDebugEnabled()) {
								log.debug("Got ctag from bean property:" + nameProp + "  ctag=" + rawId);
							}

							break;
						}
					}
					if (rawId == null) {
						// last ditch effort, use latest mod date on the collection or any member
						rawId = deriveCtag(col);
						if (log.isInfoEnabled()) {
							log.debug("Derived ctag from directory members. This is not recommended, you should implement an @CTag method. Ctag=" + rawId);
						}
					}

				}
			} else {
				rawId = cm.method.invoke(cm.controller, source);
				if (log.isDebugEnabled()) {
					log.debug("Got ctag from annotated method. ctag=" + rawId);
				}
			}
			if (rawId != null) {
				String s = rawId.toString();
				if (s.length() == 0) {
					log.warn("CTAG value is blank");
				}
				return s;
			} else {
				log.warn("CTAG value is null");
				return null;
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception executing " + getClass() + " - " + source.getClass(), e);
		}
	}
}
