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
package com.myastronomy.resource;

import com.myastronomy.ChildUtils;
import com.myastronomy.model.UniverseDao;
import com.myastronomy.model.UniverseDao.Galaxy;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.MakeCollectionableResource;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.List;


public class RootUniverseResource extends AbstractResource implements MakeCollectionableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RootUniverseResource.class);
    private final UniverseDao universeDao;
    private ArrayList<Resource> children;
    
    public RootUniverseResource(UniverseDao universeDao) {
        this.universeDao = universeDao;
    }

    @Override
    public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
        UniverseDao.Galaxy s = universeDao.addGalaxy(newName);        
        GalaxyResource r = new GalaxyResource(this, s);
        getChildren();
        children.add(r);
        return r;
    }    
    
    @Override
    public List<? extends Resource> getChildren() {
        if( children == null ) {
            children = new ArrayList<Resource>();
            for( Galaxy g : universeDao.getGalaxies()) {
                children.add(new GalaxyResource(this, g));
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
