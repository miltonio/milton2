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

import io.milton.http.values.SupportedCalendarComponentList;
import io.milton.resource.CalendarResource;

/**
 *
 * @author brad
 */
public class AnnoCalendarResource extends AnnoCollectionResource implements CalendarResource{

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
		return SupportedCalendarComponentList.VEVENT_ONLY;
	}

	@Override
	public String getCTag() {
		return annoFactory.cTagAnnotationHandler.execute(this);
	}

}
