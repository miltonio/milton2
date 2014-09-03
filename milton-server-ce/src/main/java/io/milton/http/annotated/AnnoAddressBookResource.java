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

import io.milton.common.InternationalizedString;
import io.milton.http.values.AddressDataTypeList;
import io.milton.http.values.Pair;
import io.milton.resource.AddressBookResource;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author brad
 */
public class AnnoAddressBookResource extends AnnoCollectionResource implements AddressBookResource{

	public AnnoAddressBookResource(AnnotationResourceFactory outer, Object source, AnnoCollectionResource parent) {
		super(outer, source, parent);
	}

	@Override
	public boolean is(String type) {
		if( type.equalsIgnoreCase("addressBook")) {
			return true;
		}
		return super.is(type);
	}
				
	@Override
	public String getCTag() {
		return annoFactory.cTagAnnotationHandler.execute(this);
	}

	@Override
	public InternationalizedString getDescription() {
		return new InternationalizedString(Locale.getDefault().getLanguage(), getDisplayName());
	}

	@Override
	public void setDescription(InternationalizedString description) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<Pair<String, String>> getSupportedAddressData() {
        AddressDataTypeList supportedAddresses = new AddressDataTypeList();
        supportedAddresses.add(new Pair<String, String>("text/vcard", "3.0"));
        return supportedAddresses;
	}

	@Override
	public Long getMaxResourceSize() {
		return 102400L;
	}

}
