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

package io.milton.principal;

import javax.xml.namespace.QName;

/**
 * Marker interface to identify those classes which can act as a Principal. This can be a user or a group
 * 
 * An absolute minimum requirement for a Principal is to be able to identify it, and
 * this is represented by the sole method on this interface which is getIdentifier.
 * 
 * Note that some Principals are defined by the webdav protocol so must be subject
 * to explicit processing requirements. For this reason the principle identifier
 * is a structured type which can be associated with a namespace and well known
 * names within that namespace.
 * 
 *
 * There are 2 types of Principal
 *  - Discrete: identifies a particular resource, which might be a user or group. Users are always discrete, groups may be
 * physical resources in which case they are represented as discrete resources. But groups may also be dynamically evaluated..
 *  - DAV: a dynamically evaluated group as defined by the ACL spec. Eg the Authenticated group matches any user
 * with an authenticated request. A dynamically evaluated group defined by the protocol is not
 * a physical resource do does not have a corresponding href
 *
 * @author brad
 */
public interface Principal {

    /**
     * A means to identify the principle to webdav clients
     *
     * @return
     */
    PrincipleId getIdenitifer();

    /**
	 * A principleId represents a unique identifier for a principle. It consists
	 * of 2 parts;
	 *  - type
	 *  - value (optional)
	 * 
	 * The type says this identifier identifies a physical resource or some construct
	 * defined by the ACL protocol. If the principal is a physical resource the
	 * type will be a Qualified Name with a namespace of D: (ie webdav) and a local
	 * name of href (which means the id is a href).
	 * 
	 * If the identifier identifies a protocol contruct the type will identify
	 * which construct (which must be known to the protocol and hence to milton), such
	 * as D:all, meaning all users
	 * 
     * Eg
     * <D:href>http://www.example.com/acl/groups/maintainers</D:href>
     * For a specific user or group
     *
     * or
     *
     * <D:all/>  - for a group which represents all authenticated users, and is defined
     * by the DAV standard
     *
     */
    public static interface PrincipleId {
        /**
         * Eg D:href or D:all
         *
         * @return
         */
        QName getIdType();

        /**
         * Eg null for an idType of "D:all", or http://blah.com/users/sam for "D:href"
         *
         * @return
         */
        String getValue();
    }
}
