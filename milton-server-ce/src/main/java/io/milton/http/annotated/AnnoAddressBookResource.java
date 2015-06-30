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
