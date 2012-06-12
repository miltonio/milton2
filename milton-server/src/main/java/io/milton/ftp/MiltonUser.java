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

import java.util.List;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiltonUser implements User {

    private static final Logger log = LoggerFactory.getLogger( MiltonUser.class );

    final Object user;
    final String name;
    final String domain;

    public MiltonUser( Object user, String miltonUserName, String domain) {
        super();
        if( user == null ) throw new IllegalArgumentException( "no user object provided");
        this.user = user;
        this.name = miltonUserName;
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public List<Authority> getAuthorities() {
        return null;
    }

    /**
     *
     * @return - the security implementation specific user object returned
     * by authentication
     */
    public Object getUser() {
        return user;
    }

    /**
     * {@inheritDoc}
     */
    public List<Authority> getAuthorities( Class<? extends Authority> clazz ) {
        return null;
    }

    /**
     * Note that real authorisation is done by MiltonFtpFile
     * 
     * @param request
     * @return
     */
    public AuthorizationRequest authorize( AuthorizationRequest request ) {
        log.debug( "authorize: " + request.getClass() );
        return request;
    }

    public int getMaxIdleTime() {
        return 3600;
    }

    public boolean getEnabled() {
        return true;
    }

    public String getHomeDirectory() {
        return "/";
    }
}
