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

package io.milton.http.caldav.demo;

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
    public List<LdapContact> galFind(Condition condition, int sizeLimit) {
        log.trace("galFind");
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
