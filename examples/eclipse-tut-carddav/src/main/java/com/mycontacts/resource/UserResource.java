/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mycontacts.resource;

import com.mycontacts.ChildUtils;
import com.mycontacts.model.ContactsDao;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.values.HrefList;
import io.milton.principal.CardDavPrincipal;
import io.milton.principal.HrefPrincipleId;
import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class UserResource extends AbstractResource implements CardDavPrincipal, CollectionResource{

    private final ContactsDao.User user;
    
    private ArrayList<Resource> children;
    
    public UserResource(ContactsDao.User user, RootResource parent) {
        super(parent, parent.contactManager);
        this.user = user;
    }

    @Override
    public String getName() {
        return user.getUserName();
    }

    @Override
    public HrefList getAddressBookHomeSet() {
        return HrefList.asList(getHref() + "abs/"); // the address books folder
    }

    @Override
    public String getAddress() {
        return getHref() + "abs/";
    }

    @Override
    public String getPrincipalURL() {
        return getHref();
    }

    @Override
    public PrincipleId getIdenitifer() {
        return new HrefPrincipleId(getHref());
    }

    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        return ChildUtils.child(childName, getChildren());
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        if( children == null ) {
            children = new ArrayList<Resource>();
            children.add(new UserAddressBookResource(this));
        }
        return children;        
    }
    
}
