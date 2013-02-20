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

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.values.HrefList;
import io.milton.http.values.SupportedCalendarComponentListsSet;
import io.milton.principal.CalDavPrincipal;
import io.milton.principal.CardDavPrincipal;
import io.milton.principal.DiscretePrincipal;
import io.milton.principal.HrefPrincipleId;
import io.milton.resource.Resource;

/**
 *
 * @author brad
 */
public class AnnoPrincipalResource extends AnnoCollectionResource implements DiscretePrincipal, CalDavPrincipal, CardDavPrincipal{

	public AnnoPrincipalResource(AnnotationResourceFactory outer, Object source, AnnoCollectionResource parent) {
		super(outer, source, parent);
	}

	@Override
	public HrefList getCalendarHomeSet() throws NotAuthorizedException, BadRequestException{
		HrefList list = new HrefList(); 
		for( Resource r : getChildren()) {
			if( r instanceof AnnoCollectionResource) {
				AnnoCollectionResource col = (AnnoCollectionResource) r;
				if( annoFactory.calendarsAnnotationHandler.hasCalendars(col.getSource()) ) {
					list.add(col.getHref());
				}
			}
		}
		return list;
	}
	@Override
	public HrefList getAddressBookHomeSet() {
		throw new UnsupportedOperationException("Not supported yet.");
	}	
	
	@Override
	public String getPrincipalURL() {
		return getHref();
	}

	@Override
	public PrincipleId getIdenitifer() {
		return new HrefPrincipleId(getHref());
	}


	@Override
	public HrefList getCalendarUserAddressSet() {
		return HrefList.asList(getHref());
	}

	@Override
	public String getScheduleInboxUrl() {
		return null;
	}

	@Override
	public String getScheduleOutboxUrl() {
		return null;
	}

	@Override
	public String getDropBoxUrl() {
		return null;
	}

	@Override
	public SupportedCalendarComponentListsSet getSupportedComponentSets() {
		return SupportedCalendarComponentListsSet.EVENTS_ONLY;
	}

	@Override
	public String getAddress() {
		return getHref();
	}
	
}
