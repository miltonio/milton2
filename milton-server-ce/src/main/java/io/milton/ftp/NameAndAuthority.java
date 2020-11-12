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

package io.milton.ftp;

/**
 * Parse a string into three components
 *  - username
 *  - domain
 *  - authority
 *
 * The domain field is to support servers which serve multiple domains, where the user list
 * is defined per domain.
 *
 * The authority field is to support hierarchies of domains, where the user from
 * an owning domain will get access to child domains
 *
 * @author brad
 */
public class NameAndAuthority {

    public static final String DELIM_HOST = "#";
    public static final String DELIM_AUTHORITY = ":";

    /**
     * Valid forms:
     * 
     * joe - domain and authority are null
     * joe#www.joe.com - domain and authority are 'www.joe.com'
     * joe#admin.joe.com:www.joes-client.com - authority is 'admin.joe.com', domain is 'www.joes-client.com'
     *
     * @param user - a string in one of the above forms
     * @return
     */
    public static NameAndAuthority parse( String user ) {
        if( user == null ) return new NameAndAuthority( null );
        int pos = user.indexOf( "#" );
        if( pos < 0 ) {
            return new NameAndAuthority( user );
        } else {
            String name = user.substring( 0, pos );
            String authority = user.substring( pos + 1);
            pos = authority.indexOf( DELIM_AUTHORITY);
            if( pos < 0 ) {
                return new NameAndAuthority( name, authority,authority );
            } else {
                String domain = authority.substring( pos+1);
                authority = authority.substring( 0, pos);
                return new NameAndAuthority( name, authority, domain );
            }
        }
    }
    /**
     * The username part. E.g. brad
     */
    public final String name;
    
    /**
     * The domain they are logging into. Optional. E.g. milton.ettrema.com
     */
    public final String domain;

    /**
     * The name of the authority which grants access. Optional E.g. www.ettrema.com
     *
     * This is typically a domain which owns the domain being logged into.
     *
     * If not given explicitly defaults to the domain
     */
    public final String authority;

    public NameAndAuthority( String name ) {
        this( name, null, null );
    }

    public NameAndAuthority( String name, String authority, String domain ) {
        this.name = name;
        this.authority = authority;
        this.domain = domain;
    }

    public String toMilton() {
        if( authority == null ) {
            return name;
        } else {
            return name + "@" + authority;
        }
    }
}
