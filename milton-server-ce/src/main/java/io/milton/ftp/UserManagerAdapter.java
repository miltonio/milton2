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

package io.milton.ftp;

import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.Resource;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Wraps a UserManager from apache FTP to provide a UserManager for
 * milton
 *
 *
 * @author brad
 */
public class UserManagerAdapter implements UserManager {

    private static final Logger log = LoggerFactory.getLogger( UserManagerAdapter.class );
    private final ResourceFactory resourceFactory;
    private final UserService userService;

    public UserManagerAdapter( ResourceFactory resourceFactory, UserService userLocator ) {
        this.resourceFactory = resourceFactory;
        this.userService = userLocator;
    }

    public MiltonUser getUserByName( String fqn ) throws FtpException {
        NameAndAuthority naa = NameAndAuthority.parse( fqn );
        if( naa.domain == null ) {
            log.warn( "invalid login. no domain specified. use form: user#domain" );
            return null;
        }

        return userService.getUserByName( naa.toMilton(), naa.domain );
    }

    public String[] getAllUserNames() throws FtpException {
        return userService.getAllUserNames();
    }

    @Override
    public void delete( String name ) throws FtpException {
        userService.delete( name );
    }

    @Override
    public void save( User user ) throws FtpException {
        userService.save( (MiltonUser) user );
    }

    @Override
    public boolean doesExist( String name ) throws FtpException {
        return userService.doesExist( name );
    }

    @Override
    public User authenticate( Authentication authentication ) throws AuthenticationFailedException {
        if( authentication instanceof UsernamePasswordAuthentication ) {
            UsernamePasswordAuthentication upa = (UsernamePasswordAuthentication) authentication;
            String user = upa.getUsername();
            String password = upa.getPassword();
            log.debug( "authenticate: " + user );
            NameAndAuthority naa = NameAndAuthority.parse( user );
            if( naa.domain == null ) {
                log.warn( "invalid login. no domain specified. use form: user#domain" );
                return null;
            }
            Resource hostRoot;
            try {
                hostRoot = resourceFactory.getResource( naa.domain, "/" );
            } catch (NotAuthorizedException ex) {
                throw new RuntimeException(ex);
            } catch (BadRequestException ex) {
                throw new RuntimeException(ex);
            }
            if( hostRoot == null ) {
                log.warn( "failed to find root for domain: " + naa.domain );
                return null;
            }

            Object oUser = hostRoot.authenticate( naa.toMilton(), password );
            if( oUser != null ) {
                return new MiltonUser( oUser, naa.toMilton(), naa.domain );
            } else {
                log.debug( "authentication failed: " + user );
                return null;
            }
        } else if( authentication instanceof AnonymousAuthentication ) {
            log.debug( "anonymous login not supported" );
            return null;
        } else {
            log.warn( "unknown authentication type: " + authentication.getClass() );
            return null;
        }
    }

    public String getAdminName() throws FtpException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public boolean isAdmin( String arg0 ) throws FtpException {
        return false;
    }
}

