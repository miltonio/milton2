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
    }

    public Object getUserByName( String name ) {
        String actualPassword = nameAndPasswords.get( name );
        if( actualPassword != null ) return name;
        return null;
    }



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

