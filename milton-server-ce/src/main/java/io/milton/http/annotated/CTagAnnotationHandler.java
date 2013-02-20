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
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author brad
 */
public class CTagAnnotationHandler extends AbstractAnnotationHandler {

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
				java.lang.reflect.Method m = outer.findMethodForAnno(source.getClass(), annoClass);
				if (m != null) {
					rawId = m.invoke(source, (Object) null);
				} else {
					for (String nameProp : CTAG_PROP_NAMES) {
						if (PropertyUtils.isReadable(source, nameProp)) {
							Object oPropVal = PropertyUtils.getProperty(source, nameProp);
							rawId = oPropVal;
							break;
						}
					}
					if (rawId == null) {
						// last ditch effort, use latest mod date on the collection or any member
						Date latest = col.getModifiedDate();
						if (latest != null) {
							for (CommonResource r : col.getChildren()) {
								Date d = r.getModifiedDate();
								if (latest == null || d.after(latest)) {
									latest = d;
								}
							}
						}
						if( latest != null ) {
							rawId = "T" + latest.getTime();
						}
					}
				}
			} else {
				rawId = cm.method.invoke(cm.controller, source);
			}
			if (rawId != null) {
				return rawId.toString();
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception executing " + getClass() + " - " + source.getClass());
		}
	}
}
