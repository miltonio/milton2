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

package io.milton.http.acl;

import io.milton.http.AccessControlledResource;
import io.milton.http.AccessControlledResource.Priviledge;
import io.milton.http.Auth;
import io.milton.http.Handler;
import io.milton.http.HttpExtension;
import io.milton.http.HttpManager;
import io.milton.http.http11.CustomPostHandler;
import io.milton.http.values.HrefList;
import io.milton.http.webdav.PropertyMap;
import io.milton.http.webdav.PropertyMap.StandardProperty;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.property.PropertySource;
import io.milton.http.caldav.PrincipalSearchPropertySetReport;
import io.milton.resource.PropFindableResource;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * See http://webdav.org/specs/rfc3744.html
 *
 * @author brad
 */
public class ACLProtocol implements HttpExtension, PropertySource {

	private static final Logger log = LoggerFactory.getLogger(ACLProtocol.class);
	private final PropertyMap propertyMap;

	public ACLProtocol(WebDavProtocol webDavProtocol) {
		propertyMap = new PropertyMap(WebDavProtocol.NS_DAV.getName());
		propertyMap.add(new PrincipalUrl());
		propertyMap.add(new PrincipalCollectionSetProperty());
		propertyMap.add(new CurrentUserPrincipalProperty());
                propertyMap.add(new CurrentUserPrivledges());    
                
		log.debug("registering the ACLProtocol as a property source");
		webDavProtocol.addPropertySource(this);
		//Adding supported reports
		webDavProtocol.addReport(new PrincipalSearchPropertySetReport());
	}

	/**
	 * No methods currently defined
	 * 
	 * @return
	 */
	@Override
	public Set<Handler> getHandlers() {
		return Collections.EMPTY_SET;
	}

	@Override
	public Object getProperty(QName name, Resource r) {
		log.debug("getProperty: " + name.getLocalPart());
		return propertyMap.getProperty(name, r);
	}

	@Override
	public void setProperty(QName name, Object value, Resource r) {
		log.debug("setProperty: " + name.getLocalPart());
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public PropertyMetaData getPropertyMetaData(QName name, Resource r) {
		log.debug("getPropertyMetaData: " + name.getLocalPart());
		return propertyMap.getPropertyMetaData(name, r);
	}

	@Override
	public void clearProperty(QName name, Resource r) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<QName> getAllPropertyNames(Resource r) {
		log.debug("getAllPropertyNames");
		List<QName> list = new ArrayList<QName>();
		list.addAll(propertyMap.getAllPropertyNames(r));
		return list;
	}

	@Override
	public List<CustomPostHandler> getCustomPostHandlers() {
		return null;
	}

	class PrincipalUrl implements StandardProperty<HrefList> {

		@Override
		public String fieldName() {
			return "principal-URL";
		}

		@Override
		public HrefList getValue(PropFindableResource res) {
			if (res instanceof AccessControlledResource) {
				AccessControlledResource acr = (AccessControlledResource) res;
				String url = acr.getPrincipalURL();
				if (url != null) {					
					HrefList listOfOne = new HrefList();
					listOfOne.add(url);
					return listOfOne;
				} else {
					return null;
				}
			} else {
				log.warn("requested property 'principal-url', but resource doesnt implement AccessControlledResource: " + res.getClass().getCanonicalName());
				return null;
			}
		}

		@Override
		public Class<HrefList> getValueClass() {
			return HrefList.class;
		}
	}

	/*
	<principal-collection-set>
	<href>/principals/</href>
	</principal-collection-set>
	 */
	class PrincipalCollectionSetProperty implements StandardProperty<HrefList> {

		@Override
		public String fieldName() {
			return "principal-collection-set";
		}

		@Override
		public HrefList getValue(PropFindableResource res) {
			if (res instanceof AccessControlledResource) {
				AccessControlledResource acr = (AccessControlledResource) res;
				return acr.getPrincipalCollectionHrefs();
			} else {
				return null;
			}

		}

		@Override
		public Class<HrefList> getValueClass() {
			return HrefList.class;
		}
	}

	class CurrentUserPrincipalProperty implements StandardProperty<HrefList> {

		@Override
		public String fieldName() {
			return "current-user-principal";
		}

		@Override
		public HrefList getValue(PropFindableResource res) {
			Auth auth = HttpManager.request().getAuthorization();
			if (auth == null || auth.getTag() == null) {
				return null;
			} else {
				Object user = auth.getTag();
				if (user instanceof DiscretePrincipal) {
					DiscretePrincipal p = (DiscretePrincipal) user;
					HrefList hrefs = new HrefList();
					hrefs.add(p.getPrincipalURL());
					return hrefs;
				} else {
					return null;
				}
			}
		}

		@Override
		public Class<HrefList> getValueClass() {
			return HrefList.class;
		}
	}

	class CurrentUserPrivledges implements StandardProperty<PriviledgeList> {

		@Override
		public String fieldName() {
			return "current-user-privilege-set";
		}

		@Override
		public PriviledgeList getValue(PropFindableResource res) {
			if (res instanceof AccessControlledResource) {
				AccessControlledResource acr = (AccessControlledResource) res;
				Auth auth = HttpManager.request().getAuthorization();
				List<Priviledge> list = acr.getPriviledges(auth);
				PriviledgeList privs = new PriviledgeList(list);
				return privs;
			} else {
				return null;
			}
		}

		@Override
		public Class<PriviledgeList> getValueClass() {
			return PriviledgeList.class;
		}
	}
}
