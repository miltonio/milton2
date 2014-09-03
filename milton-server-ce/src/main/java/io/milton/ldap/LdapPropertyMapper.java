/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.milton.ldap;

import io.milton.common.LogUtils;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.values.ValueAndType;
import io.milton.http.webdav.PropFindPropertyBuilder;
import io.milton.resource.PropFindableResource;
import io.milton.resource.Resource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class LdapPropertyMapper {

	private static final Logger log = LoggerFactory.getLogger(SimpleLdapFilter.class);
	private final PropFindPropertyBuilder propertyBuilder;
	private final Map<QName, String> mapQNameDavToLdap = new ConcurrentHashMap<QName, String>();
	private final Map<String, QName> mapQNameLdapToDav = new ConcurrentHashMap< String, QName>();
	private final Map<String, String> mapLocalNameDavToLdap = new ConcurrentHashMap<String, String>();
	private final Map<String, String> mapLocalNameLdapToDav = new ConcurrentHashMap< String, String>();
	
	private String ldapNameSpace = "ldap";

	public LdapPropertyMapper(PropFindPropertyBuilder propertyBuilder) {
		this.propertyBuilder = propertyBuilder;
		addLocalNameMapping("name", "uid");
		addLocalNameMapping("surName", "surname");
		addLocalNameMapping("surName", "sn");
		addLocalNameMapping("commonName", "cn");
		addLocalNameMapping("country", "c");
		addLocalNameMapping("friendlyCountryName", "co");
		addLocalNameMapping("givenName", "gn");
		addLocalNameMapping("givenName", "givenname");
		addLocalNameMapping("organizationName", "o");
		addLocalNameMapping("commonName", "nsaimid");		
		
	}

	private void addLocalNameMapping(String davName, String ldapName) {
		mapLocalNameDavToLdap.put(davName, ldapName);
		mapLocalNameLdapToDav.put(ldapName, davName);
	}
	
	public ValueAndType getProperty(QName field, Resource resource) throws NotAuthorizedException, BadRequestException {
		ValueAndType vt = propertyBuilder.getProperty(field, resource);
		return vt;
	}

	public QName mapToDavProp(String s) {
		QName q = mapQNameLdapToDav.get(s);
		if (q != null) {
			return q;
		} else {
			String mappedLocalName = mapLocalNameLdapToDav.get(s);
			if( mappedLocalName != null ) {
				return new QName(ldapNameSpace, mappedLocalName);
			}
			return new QName(ldapNameSpace, s);
		}
	}

	public String mapToLdapProp(QName p) {
		String q = mapQNameDavToLdap.get(p);
		if (q != null) {
			return q;
		} else {
			String mappedLocalName = mapLocalNameDavToLdap.get(p.getLocalPart());
			if( mappedLocalName != null ) {
				return mappedLocalName;
			}
			return p.getLocalPart();
		}
	}

	public String getLdapPropertyValue(String prop, Resource resource) throws BadRequestException {
		QName qn = mapToDavProp(prop);
		ValueAndType vt;
		try {
			vt = getProperty(qn, resource);
		} catch (NotAuthorizedException ex) {
			log.trace("property access not authorised");
			vt = null;
		}
		Object propValue;
		if (vt != null && vt.getValue() != null) {
			propValue = vt.getValue();
			return propValue.toString();
		}
		LogUtils.trace(log, "getLdapPropertyValue: property not found: ldap property: ", prop, " - dav prop: ", qn, "resource: ", resource.getClass());
		return null;
	}

	public Set<LdapMappedProp> mapProperties(boolean returnAllAttributes, Set<String> returningAttributes, PropFindableResource res) throws NotAuthorizedException, BadRequestException {
		if (returnAllAttributes) {
			Set<QName> davProps = propertyBuilder.findAllProps(res);
			Set<LdapMappedProp> mapped = new HashSet<LdapMappedProp>();
			for (QName p : davProps) {
				String ldapProp = mapToLdapProp(p);
				LdapMappedProp ldapMappedProp = new LdapMappedProp(ldapProp, p);
				mapped.add(ldapMappedProp);
			}
			return mapped;
		} else {
			Set<LdapMappedProp> mapped = new HashSet<LdapMappedProp>();
			for (String s : returningAttributes) {
				QName qn = mapToDavProp(s);
				LdapMappedProp ldapMappedProp = new LdapMappedProp(s, qn);
				mapped.add(ldapMappedProp);
			}
			return mapped;
		}
	}

	public class LdapMappedProp {

		String ldapName;
		QName mappedName;

		public LdapMappedProp(String ldapName, QName mappedName) {
			this.ldapName = ldapName;
			this.mappedName = mappedName;
		}
	}
}
