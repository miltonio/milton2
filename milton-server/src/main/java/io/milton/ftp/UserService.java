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

/**
 *
 * @author brad
 */
public interface UserService {

    /**
     * don't need it
     *
     * @param name
     */
    public void delete( String name );

    /**
     * * don't need it
     *
     * @param name
     * @return
     */
    public boolean doesExist( String name );

    /**
     * * don't need it
     *
     * @return
     */
    public String[] getAllUserNames();

    /**
     *
     * @param name - milton form of the username E.g. user@authority
     * @param domain - the domain to login into
     * @return
     */
    public MiltonUser getUserByName( String name, String domain );

    /**
     * Save the user. You don't need to implement this
     * @param user
     */
    public void save( MiltonUser user );

}
