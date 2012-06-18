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

package io.milton.resource;

import io.milton.http.Auth;
import io.milton.http.values.HrefList;
import io.milton.principal.Principal;
import java.util.List;
import java.util.Map;

/**
 * 5.4.1. Example: Retrieving the User's Current Set of Assigned Privileges
 *
 * Continuing the example from Section 5.3.1, this example shows a client
 * requesting the DAV:current-user-privilege-set property from the resource with
 * URL http://www.example.com/papers/. The username of the principal making the
 * request is "khare", and Digest authentication is used in the request. The
 * principal with username "khare" has been granted the DAV:read privilege.
 * Since the DAV:read privilege contains the DAV:read-acl and
 * DAV:read-current-user-privilege-set privileges (see Section 5.3.1), the
 * principal with username "khare" can read the ACL property, and the
 * DAV:current-user-privilege-set property. However, the DAV:all, DAV:read-acl,
 * DAV:write-acl and DAV:read-current-user-privilege-set privileges are not
 * listed in the value of DAV:current-user-privilege-set, since (for this
 * example) they are abstract privileges. DAV:write is not listed since the
 * principal with username "khare" is not listed in an ACE granting that
 * principal write permission.
 *
 * >> Request <<
 *
 * PROPFIND /papers/ HTTP/1.1 Host: www.example.com Content-type: text/xml;
 * charset="utf-8" Content-Length: xxx Depth: 0 Authorization: Digest
 * username="khare", realm="users@example.com", nonce="...", uri="/papers/",
 * response="...", opaque="..."
 *
 * <?xml version="1.0" encoding="utf-8" ?> <D:propfind xmlns:D="DAV:"> <D:prop>
 * <D:current-user-privilege-set/> </D:prop> </D:propfind>
 *
 * >> Response <<
 *
 * HTTP/1.1 207 Multi-Status Content-Type: text/xml; charset="utf-8"
 * Content-Length: xxx
 *
 * <?xml version="1.0" encoding="utf-8" ?> <D:multistatus xmlns:D="DAV:">
 * <D:response> <D:href>http://www.example.com/papers/</D:href> <D:propstat>
 * <D:prop> <D:current-user-privilege-set> <D:privilege><D:read/></D:privilege>
 * </D:current-user-privilege-set> </D:prop> <D:status>HTTP/1.1 200
 * OK</D:status> </D:propstat> </D:response> </D:multistatus>
 *
 *
 *
 * @author alex
 */
public interface AccessControlledResource extends Resource {

    public enum Priviledge {

        READ,
        WRITE,
        READ_ACL,
        WRITE_ACL,
        UNLOCK,
        READ_CURRENT_USER_PRIVILEDGE,
        WRITE_PROPERTIES,
        WRITE_CONTENT,
        BIND,
        UNBIND,
        ALL
    }

    /**
     * A URL which identifies the principal owner of this resource
     *
     * See http://greenbytes.de/tech/webdav/rfc3744.html#PROPERTY_principal-URL
     *
     * @return
     */
    String getPrincipalURL();

    /**
     * Return the list of privlidges which the current user (given by the auth
     * object) has access to, on this resource.
     *
     * @param auth
     * @return
     */
    List<Priviledge> getPriviledges(Auth auth);

    /**
     * Get all allowed priviledges for all principals on this resource. Note
     * that a principal might be a user, a group, or a built-in webdav group
     * such as AUTHENTICATED
     *
     * @return
     */
    Map<Principal, List<Priviledge>> getAccessControlList();

    /**
     * Set the allowed Priviledges for all principals to the given list.
     * Existing priviledges must be removed or updated as required so that the
     * given list is the complete list for this resource
     *
     *
     * @param privs
     */
    void setAccessControlList(Map<Principal, List<Priviledge>> privs);

    /**
     * Return the hrefs (either fully qualified URLs or absolute paths) to the
     * collections which contain principals. This is to allow user agents to
     * display a list of users to display.
     *
     * Most implementations will only have a single value which will be the path
     * to the users folder. Eg:
     *
     * return Arrays.asList("/users/");
     *
     * @return - a list of hrefs
     */
    HrefList getPrincipalCollectionHrefs();
}
