/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * 
 */

package io.milton.sso;

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
