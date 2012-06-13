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

package io.milton.servlet;

import io.milton.sso.SsoSessionProvider;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * This SsoSessionProvider works by keeping a track of active sessions, and looking
 * up sessions by the session id, which forms the leading path of a SSO path.
 * 
 * Eg:
 * 
 * /ABC123/MyDocuments/adoc.doc
 * 
 * Note that to be secure this should be used over SSL
 *
 * @author brad
 */
public class ServletSsoSessionProvider implements SsoSessionProvider, HttpSessionListener {

	/**
	 * Note, one shared map across all instances of ServletSsoSessionProvider!
	 * 
	 */
	private static final Map<String,HttpSession> mapOfSessions = new ConcurrentHashMap<String, HttpSession>();
	
	private String userSessionVariableName = "user";
	
	
	
	
	public Object getUserTag(String firstComp) {
		HttpSession sess = mapOfSessions.get(firstComp);
		if( sess == null ) {
			return null;
		} else {
			Object oUser = sess.getAttribute(userSessionVariableName);
			return oUser;
		}
	}

	public void sessionCreated(HttpSessionEvent hse) {
		String id = hse.getSession().getId();
		mapOfSessions.put(id, hse.getSession());
	}

	public void sessionDestroyed(HttpSessionEvent hse) {
		String id = hse.getSession().getId();
		mapOfSessions.remove(id);
	}

	public String getUserSessionVariableName() {
		return userSessionVariableName;
	}

	public void setUserSessionVariableName(String userSessionVariableName) {
		this.userSessionVariableName = userSessionVariableName;
	}
		
	
}
