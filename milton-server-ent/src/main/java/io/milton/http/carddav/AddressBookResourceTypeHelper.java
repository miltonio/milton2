/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.milton.http.carddav;

import io.milton.http.webdav.ResourceTypeHelper;
import io.milton.resource.AddressBookResource;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class AddressBookResourceTypeHelper implements ResourceTypeHelper {

    private static final Logger log = LoggerFactory.getLogger(AddressBookResourceTypeHelper.class);
    private final ResourceTypeHelper wrapped;

    public AddressBookResourceTypeHelper(ResourceTypeHelper wrapped) {
        log.debug("CalendarResourceTypeHelper constructed :" + wrapped.getClass().getSimpleName());
        this.wrapped = wrapped;
    }

    @Override
    public List<QName> getResourceTypes(Resource r) {
        if (log.isTraceEnabled()) {
            log.trace("getResourceTypes:" + r.getClass().getCanonicalName());
        }
        QName qn;
        List<QName> list = wrapped.getResourceTypes(r);
		
        if (r instanceof AddressBookResource) {
            log.trace("getResourceTypes: is a calendar");
            qn = new QName(CardDavProtocol.CARDDAV_NS, "addressbook");
            if (list == null) {
                list = new ArrayList<QName>();
            }
            list.add(qn);
        }
        return list;
    }

    /**
     *
     *
     * @param r
     * @return
     */
    @Override
    public List<String> getSupportedLevels(Resource r) {
        log.debug("getSupportedLevels");
        List<String> list = wrapped.getSupportedLevels(r);
//        if (r instanceof AddressBookResource) {
		addIfNotPresent(list,"3");			
		addIfNotPresent(list,"addressbook");	
		addIfNotPresent(list,"extended-mkcol");			
//        }
        return list;
    }
	
	
	private void addIfNotPresent(List<String> list, String s) {
		if( !list.contains(s)) {
			list.add(s);
		}
	}	
}
