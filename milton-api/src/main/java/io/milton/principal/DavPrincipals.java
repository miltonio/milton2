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

import io.milton.http.Auth;
import io.milton.resource.Resource;
import javax.xml.namespace.QName;

/**
 * Used to represent aggregated principals defined by the ACL spec
 *
 * Eg D:all, D:authenticated, D:unauthenticated
 *
 *
 * @author brad
 */
public class DavPrincipals {

    public static DavPrincipal All = new AllDavPrincipal();
    public static DavPrincipal AUTHENTICATED = new AllDavPrincipal();
    public static DavPrincipal UNAUTHENTICATED = new UnAuthenticatedDavPrincipal();

    public abstract static class AbstractDavPrincipal implements DavPrincipal {

        private final PrincipleId id;
        private final QName qname;

        public AbstractDavPrincipal( String name ) {
            this.qname = new QName( "DAV:", name );
            this.id = new PrincipleId() {

                public QName getIdType() {
                    return qname;
                }

                public String getValue() {
                    return null;
                }
            };
        }

        public PrincipleId getIdenitifer() {
            return id;
        }
    }

    public static class AllDavPrincipal extends DavPrincipals.AbstractDavPrincipal {

        AllDavPrincipal() {
            super( "all" );
        }

        public boolean matches( Auth auth, Resource current ) {
            return true;
        }
    }

    public static class AuthenticatedDavPrincipal extends DavPrincipals.AbstractDavPrincipal {

        AuthenticatedDavPrincipal() {
            super( "authenticated" );
        }

        public boolean matches( Auth auth, Resource current ) {
            return auth.getTag() != null;
        }
    }

    public static class UnAuthenticatedDavPrincipal extends DavPrincipals.AbstractDavPrincipal {

        UnAuthenticatedDavPrincipal() {
            super( "unauthenticated" );
        }

        public boolean matches( Auth auth, Resource current ) {
            return auth.getTag() == null;
        }
    }
}
