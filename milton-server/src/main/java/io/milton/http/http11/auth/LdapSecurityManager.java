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

package io.milton.http.http11.auth;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.resource.Resource;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * NOT TESTED YET!!!!!!
 * 
 * 
 * From here: http://www.forumeasy.com/forums/thread.jsp?tid=115170863235&fid=ldapprof5&highlight=Why+DIGEST-MD5+Authentication+Does+Work
 * 
 * Server: AD 2003
Client: JNDI application
User: cn=testuser,cn=users,dc=mydomain,dc=com
Realm: MYREALM
Passwd: (password stored in hash format)
 * 
 * The following settings works

env.put(Context.SECURITY_AUTHENTICATION, "DIGEST-MD5");
env.put(Context.SECURITY_PRINCIPAL, "testuser"); 

 * 
 * The following settings dose NOT works

env.put(Context.SECURITY_AUTHENTICATION, "DIGEST-MD5");
env.put(Context.SECURITY_PRINCIPAL, "MYREAM\\testuser"); 



The following settings dose NOT works

env.put(Context.SECURITY_AUTHENTICATION, "DIGEST-MD5");
env.put(Context.SECURITY_PRINCIPAL, "testuser@mydomain.com"); 

 *
 * @author brad
 */
public class LdapSecurityManager implements io.milton.http.SecurityManager {

	private static final Logger log = LoggerFactory.getLogger( LdapSecurityManager.class );
	private String ldapUrl = "LDAP://localhost/CN=App1,DC=FM,DC=COM";
	private String realm = "aRealm";
	private boolean enableDigest = true;
	

	@Override
	public Object authenticate(DigestResponse digestRequest) {
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapUrl);

		env.put(Context.SECURITY_AUTHENTICATION, "DIGEST-MD5");
		env.put(Context.SECURITY_PRINCIPAL, digestRequest.getUser());		
		env.put(Context.SECURITY_CREDENTIALS, digestRequest.getResponseDigest());
		DirContext ctx = null;
		try {
			ctx = new InitialDirContext(env);
			return ctx;
		} catch (NamingException ex) {
			log.warn("login failed", ex);
			return null;
		} finally {
			close(ctx);
		}

	}

	@Override
	public Object authenticate(String user, String password) {
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapUrl);

		env.put(Context.SECURITY_PRINCIPAL, user);		
		env.put(Context.SECURITY_CREDENTIALS, password);
		DirContext ctx = null;
		try {
			ctx = new InitialDirContext(env);
			return ctx;
		} catch (NamingException ex) {
			ex.printStackTrace();
			//log.warn("login failed", ex);
			return null;
		} finally {
			close(ctx);
		}
	}

	@Override
	public boolean authorise(Request request, Method method, Auth auth, Resource resource) {
		DirContext ctx = (DirContext) auth.getTag();
		return ctx != null; // TODO: check for roles in directory
	}

	@Override
	public String getRealm(String host) {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}
		
	@Override
	public boolean isDigestAllowed() {
		return enableDigest;
	}

	public void setEnableDigest(boolean enableDigest) {
		this.enableDigest = enableDigest;
	}

	public boolean isEnableDigest() {
		return enableDigest;
	}

	public String getLdapUrl() {
		return ldapUrl;
	}

	public void setLdapUrl(String ldapUrl) {
		this.ldapUrl = ldapUrl;
	}
		
	private void close(DirContext ctx) {
		if (ctx != null) {
			try {
				ctx.close();
			} catch (NamingException ex) {
			}
		}
	}
	
}
