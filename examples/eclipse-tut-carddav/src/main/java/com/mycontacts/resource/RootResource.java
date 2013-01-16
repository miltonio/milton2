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
package com.mycontacts.resource;

import com.mycontacts.ChildUtils;
import com.mycontacts.model.ContactsDao;
import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.List;


public class RootResource extends AbstractResource implements CollectionResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RootResource.class);
    private ArrayList<Resource> children;
    
    public RootResource(ContactManager contactManager) {
        super(contactManager);
    }
    
    @Override
    public List<? extends Resource> getChildren() {
        if( children == null ) {
            children = new ArrayList<Resource>();
            for( ContactsDao.User g : contactManager.getContactsDao().getUsers()) {
                children.add(new UserResource(g, this));
            }
        }
        return children;
    }

    @Override
    public Resource child(String childName) {
        return ChildUtils.child(childName, getChildren());
    }

    @Override
    public String getName() {
        return "";
    }
    
    
}
