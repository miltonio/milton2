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
