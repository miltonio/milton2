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

import io.milton.annotations.FreeBusyQuery;
import io.milton.resource.SchedulingResponseItem;
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
