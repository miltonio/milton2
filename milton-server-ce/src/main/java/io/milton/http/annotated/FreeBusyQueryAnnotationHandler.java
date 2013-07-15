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

import io.milton.annotations.FreeBusyQuery;
import io.milton.resource.ICalResource;
import io.milton.resource.SchedulingResponseItem;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class FreeBusyQueryAnnotationHandler extends AbstractAnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(FreeBusyQueryAnnotationHandler.class);

	public FreeBusyQueryAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, FreeBusyQuery.class);
	}

	public List<SchedulingResponseItem> execute(AnnoPrincipalResource principal, String icalText) {
		Object source = principal.getSource();
		try {
			ControllerMethod cm = getBestMethod(source.getClass());
			if (cm == null) {
				return null;
			} else {
				Object[] args = annoResourceFactory.buildInvokeArgs(principal, cm.method, icalText);
				List<SchedulingResponseItem> responseItems = (List<SchedulingResponseItem>) invoke(cm, principal, args);
				return responseItems;
			}
			
		} catch (Exception e) {
			throw new RuntimeException("Exception executing " + getClass() + " - " + source.getClass(), e);
		}
	}
}
