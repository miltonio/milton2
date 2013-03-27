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

import io.milton.annotations.ICalData;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author brad
 */
public class ICalDataAnnotationHandler extends AbstractAnnotationHandler {

	private final String[] CTAG_PROP_NAMES = {"ical", "icalData"};

	public ICalDataAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, ICalData.class);
	}

	public String execute(AnnoEventResource eventRes) {
		Object source = eventRes.getSource();
		try {
			Object value = null;
			ControllerMethod cm = getBestMethod(source.getClass());
			if (cm == null) {
				// look for an annotation on the source itself
				java.lang.reflect.Method m = outer.findMethodForAnno(source.getClass(), annoClass);
				if (m != null) {
					value = m.invoke(source, (Object) null);
				} else {
					for (String nameProp : CTAG_PROP_NAMES) {
						if (PropertyUtils.isReadable(source, nameProp)) {
							Object oPropVal = PropertyUtils.getProperty(source, nameProp);
							value = oPropVal;
							break;
						}
					}
				}
			} else {
				value = invoke(cm, eventRes);
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
