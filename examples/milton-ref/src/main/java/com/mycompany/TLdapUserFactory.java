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

package com.mycompany;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.ldap.Condition;
import io.milton.ldap.LdapPrincipal;
import io.milton.ldap.UserFactory;
import io.milton.resource.LdapContact;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class TLdapUserFactory implements UserFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TLdapUserFactory.class);
    private final TResourceFactory resourceFactory;

    public TLdapUserFactory(TResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }

    @Override
    public String getUserPassword(String userName) {
        TCalDavPrincipal user = TResourceFactory.findUser(userName);
        if (user == null) {
            return null;
        } else {
            return user.getPassword();
        }
    }

    @Override
    public LdapPrincipal getUser(String userName, String password) {
        TCalDavPrincipal user = TResourceFactory.findUser(userName);
        if( user == null ) {
            System.out.println("User not found: " + userName);
            return null;
        }
        if (user.authenticate(password)) {
            return user;
        } else {
            System.out.println("Passwords don't match: " + password + " != " + user.getPassword());
            return null;
        }
    }

    @Override
    public List<LdapContact> galFind(Condition condition, int sizeLimit) throws NotAuthorizedException, BadRequestException {
        log.info("galFind: " + condition);
        List<LdapContact> results = new ArrayList<LdapContact>();

        for (Resource r : resourceFactory.getUsers()) {
            if (r instanceof TCalDavPrincipal) {
                TCalDavPrincipal user = (TCalDavPrincipal) r;
                if (condition == null || condition.isMatch(user)) {
                    log.debug("searchContacts: add to results:" + user.getName());
                    results.add(user);
                    if (results.size() >= sizeLimit) {
                        break;
                    }
                }
            }
        }
        log.debug("galFind: results: " + results.size());
        return results;
    }
}
