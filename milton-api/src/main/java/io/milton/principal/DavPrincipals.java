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
