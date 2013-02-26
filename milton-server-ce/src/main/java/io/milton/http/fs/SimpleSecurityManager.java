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

package io.milton.http.fs;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.resource.Resource;
import io.milton.http.http11.auth.DigestGenerator;
import io.milton.http.http11.auth.DigestResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Has a realm and a map where the keys are user names and the values are
 * passwords
 *
 * @author brad
 */
public class SimpleSecurityManager implements io.milton.http.SecurityManager{

    private static final Logger log = LoggerFactory.getLogger(SimpleSecurityManager.class);

    private String realm;
    private Map<String,String> nameAndPasswords;
    private DigestGenerator digestGenerator;

    public SimpleSecurityManager() {
        digestGenerator = new DigestGenerator();
    }

    public SimpleSecurityManager( DigestGenerator digestGenerator ) {
        this.digestGenerator = digestGenerator;
    }

   
    public SimpleSecurityManager( String realm, Map<String,String> nameAndPasswords ) {
        this.realm = realm;
        this.nameAndPasswords = nameAndPasswords;
		digestGenerator = new DigestGenerator();
    }

    public Object getUserByName( String name ) {
        String actualPassword = nameAndPasswords.get( name );
        if( actualPassword != null ) {
			return name;
		}
        return null;
    }



	@Override
    public Object authenticate( String user, String password ) {
        log.debug( "authenticate: " + user + " - " + password);
        // user name will include domain when coming form ftp. we just strip it off
        if( user.contains( "@")) {
            user = user.substring( 0, user.indexOf( "@"));
        }
        String actualPassword = nameAndPasswords.get( user );
        if( actualPassword == null ) {
            log.debug( "user not found: " + user);
            return null;
        } else {
            boolean ok;
            if( actualPassword == null ) {
                ok = password == null || password.length()==0;
            } else {
                ok = actualPassword.equals( password);
            }
            return ok ? user : null;
        }
    }

	@Override
    public Object authenticate( DigestResponse digestRequest ) {
		if( digestGenerator == null ) {
			throw new RuntimeException("No digest generator is configured");
		}
        String actualPassword = nameAndPasswords.get( digestRequest.getUser() );
        String serverResponse = digestGenerator.generateDigest( digestRequest, actualPassword );
        String clientResponse = digestRequest.getResponseDigest();

        if( serverResponse.equals( clientResponse ) ) {
            return "ok";
        } else {
            return null;
        }
    }



	@Override
    public boolean authorise( Request request, Method method, Auth auth, Resource resource ) {
		if( auth == null ) {
			log.trace("authorise: declining because there is no auth object");
			return false;
		} else {
			if( auth.getTag() == null ) {
				log.trace("authorise: declining because there is no auth.getTag() object");
				return false;
			} else {
				log.trace("authorise: permitting because there is an authenticated user associated with this request");
				return true;
			}
		}
    }

	@Override
    public String getRealm(String host) {
        return realm;
    }

    /**
     * @param realm the realm to set
     */
    public void setRealm( String realm ) {
        this.realm = realm;
    }

    public void setNameAndPasswords( Map<String, String> nameAndPasswords ) {
        this.nameAndPasswords = nameAndPasswords;
    }

	public void setDigestGenerator(DigestGenerator digestGenerator) {
		this.digestGenerator = digestGenerator;
	}
	
	@Override
	public boolean isDigestAllowed() {
		return digestGenerator != null;
	}


//    public MiltonUser getUserByName( String name, String domain ) {
//        log.debug( "getUserByName: " + name + " - " + domain);
//        String actualPassword = nameAndPasswords.get( name );
//        if( actualPassword == null ) return null;
//        return new MiltonUser( name, name, domain );
//    }

	public DigestGenerator getDigestGenerator() {
		return digestGenerator;
	}
}

