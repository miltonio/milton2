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

import io.milton.resource.AddressResource;
import io.milton.resource.ICalResource;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class AnnoContactResource extends AnnoFileResource implements AddressResource{

	private static final Logger log = LoggerFactory.getLogger(AnnoContactResource.class);
	
	public AnnoContactResource(AnnotationResourceFactory outer, Object source, AnnoCollectionResource parent) {
		super(outer, source, parent);
	}

	@Override
	public boolean is(String type) {
		if( type.equals("contact")) {
			return true;
		}
		return super.is(type);
	}

	@Override
	public String getUniqueId() {
		String s = super.getUniqueId();
		if( s == null ) {
			log.warn("No unique ID for event class: " + source.getClass() + " Locking and other vital operations will not be available!!");
		}
		return s;
	}

	@Override
	public Date getModifiedDate() {
		Date dt = super.getModifiedDate();
		if( dt == null ) {
			log.warn("No ModifiedDate for event class: " + source.getClass() + " This will cause incorrect contact syncronisation!!!");
		}
		return dt;
	}

	@Override
	public String getContentType(String accepts) {
		return "text/vcard";
	}

	@Override
	public String getAddressData() {
		return annoFactory.contactDataAnnotationHandler.execute(this);
	}

	

	
}
