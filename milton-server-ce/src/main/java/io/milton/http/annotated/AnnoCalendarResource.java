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

import io.milton.http.values.SupportedCalendarComponentList;
import io.milton.resource.CalendarResource;
import io.milton.resource.DisplayNameResource;

/**
 *
 * @author brad
 */
public class AnnoCalendarResource extends AnnoCollectionResource implements CalendarResource, DisplayNameResource {

	public AnnoCalendarResource(AnnotationResourceFactory outer, Object source, AnnoCollectionResource parent) {
		super(outer, source, parent);
	}

	@Override
	public boolean is(String type) {
		if( type.equals("calendar")) {
			return true;
		}
		return super.is(type);
	}
	
	
			

	@Override
	public String getCalendarDescription() {
		return getDisplayName();
	}

	@Override
	public String getColor() {
		return annoFactory.calendarColorAnnotationHandler.get(this); 
	}

	@Override
	public void setColor(String color) {
		annoFactory.calendarColorAnnotationHandler.set(this, color);
	}

	@Override
	public SupportedCalendarComponentList getSupportedComponentSet() {
		return SupportedCalendarComponentList.VEVENT_VFREEBUSY;
	}

	@Override
	public String getCTag() {
		return annoFactory.cTagAnnotationHandler.execute(this);
	}

	@Override
	public String getCalendarOrder() {
		return annoFactory.calendarOrderAnnotationHandler.get(this); 
	}

	@Override
	public void setCalendarOrder(String value) {
		annoFactory.calendarOrderAnnotationHandler.set(this, value); 
	}

	@Override
	public void setDisplayName(String s) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
