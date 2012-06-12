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

package io.milton.ldap;

import io.milton.resource.LdapContact;
import io.milton.common.LogUtils;
import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.property.BeanPropertyResource;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class MemoryUserSessionFactory implements UserFactory {

	private static final Logger log = LoggerFactory.getLogger(MemoryUserSessionFactory.class);
	private final Map<String, MemoryUser> users;

	public MemoryUserSessionFactory(Map<String, MemoryUser> users) {
		this.users = users;
	}

	public MemoryUserSessionFactory() {
		this.users = new HashMap<String, MemoryUser>();
	}

	public void addUser(String name, String password, String givenName, String surname, String email) {
		MemoryUser u = new MemoryUser(name, password, givenName, surname);
		u.setMail(email);
		users.put(name, u);
	}

	public MemoryUser getUser(String userName) {
		MemoryUser u = users.get(userName);
		LogUtils.debug(log, "getUser", userName, "result=", u);
		return u;
	}

	@Override
	public String getUserPassword(String userName) {
		MemoryUser user = getUser(userName);
		if (user == null) {
			LogUtils.warn(log, "getUserPassword: user not found", userName);
			return null;
		} else {
			return user.getPassword();
		}
	}

	@Override
	public LdapPrincipal getUser(String userName, String password) {
		MemoryUser user = getUser(userName);
		if (user == null) {
			LogUtils.warn(log, "getUser: user not found", userName);
			return null;
		} else {
			if (password.equals(user.getPassword())) {
				LogUtils.debug(log, "getUser: user authentuicated ok", userName);
				return user;
			} else {
				LogUtils.warn(log, "getUser: incorrect password", userName);
				return null;
			}
		}
	}

	@Override
	public List<LdapContact> galFind(Condition condition, int sizeLimit) {
		log.trace("galFind");
		List<LdapContact> results = new ArrayList<LdapContact>();
		for (MemoryUser user : users.values()) {
			if (condition == null || condition.isMatch(user)) {
				LogUtils.debug(log, "searchContacts: add to results", user.userName);
				results.add(user);
				if (results.size() >= sizeLimit) {
					break;
				}
			}
		}
		LogUtils.debug(log, "galFind: results: ", results.size());
		return results;
	}


	/**
	 * The BeanPropertyResource annotation makes the bean properties on this class
	 * available to milton property resolution. The value is the namespace that 
	 * these properties will be mapped to.
	 * 
	 * Note that the LDAP support will, by default, map properties to the "ldap"
	 * namespace, which must correspond to the namespace used here
	 * 
	 */
	@BeanPropertyResource(value="ldap")
	public class MemoryUser extends MapContact implements LdapPrincipal, LdapContact {

		private final String userName;
		private String password;

		public MemoryUser(String alias, String password, String givenName,String surname) {
			super(alias);
			this.userName = alias;
			this.password = password;
			put("imapUid", alias);
			put("uid", alias);
			put("etag", alias + this.hashCode());
			Date dtBirth = new Date();
			String sBirth = LdapUtils.getZuluDateFormat().format(dtBirth);
			put("birth", sBirth);
			put("bday", sBirth);
			put("im", alias);
			setGivenName(givenName);
			setSurName(surname);
			put("cn", givenName + " " + surname);
		}

		@Override
		public List<LdapContact> searchContacts(Condition condition, int maxCount) {
			return Collections.EMPTY_LIST;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getEtag() {
			return this.hashCode() + "";
		}

		public String getGivenName() {
			return get("givenName");
		}

		public final void setGivenName(String givenName) {
			put("givenName", givenName);
		}

		public String getSurName() {
			return get("sn");
		}

		public final void setSurName(String surname) {
			put("sn", surname);
		}

		public String getMail() {
			return get("mail");
		}

		public void setMail(String s) {
			put("mail", s);
		}
		
		public String getCommonName() {
			return getGivenName() + " " + getSurName();
		}

		@Override
		public Date getCreateDate() {
			return null;
		}

		@Override
		public String getName() {
			return userName;
		}

		@Override
		public Object authenticate(String user, String password) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean authorise(Request request, Method method, Auth auth) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public String getRealm() {
			return "ldap";
		}

		@Override
		public Date getModifiedDate() {
			return null;
		}

		@Override
		public String checkRedirect(Request request) {
			return null;
		}
		
	}
}
