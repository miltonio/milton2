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
