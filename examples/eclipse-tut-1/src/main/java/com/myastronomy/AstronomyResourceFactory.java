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
package com.myastronomy;

import com.myastronomy.model.UniverseDao;
import com.myastronomy.resource.RootUniverseResource;
import io.milton.common.Path;
import io.milton.http.HttpManager;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.values.HrefList;
import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;
import java.util.Calendar;
import java.util.List;

/**
 * For iCal, start off by opening a calendar at
 *
 * http://localhost:8080/users/userA/ - iCal will discover the calendar inside
 * that user.
 *
 * For Mozilla clients (eg thunderbird) connect directly to the calendar url,
 * eg
 *
 * http://localhost:8080/users/userA/calendars/cal1/
 *
 * @author brad
 */
public class AstronomyResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AstronomyResourceFactory.class);
    private final UniverseDao universeDao = new UniverseDao();

    @Override
    public Resource getResource(String host, String url) throws NotAuthorizedException, BadRequestException {
        log.debug("getResource: url: " + url);
        Path path = Path.path(url);
        Resource r = find(path);
        log.debug("_found: " + r + " for url: " + url + " and path: " + path);
        return r;
    }

    private Resource find(Path path) throws NotAuthorizedException, BadRequestException {
        if (path.isRoot()) {
            RootUniverseResource r = (RootUniverseResource) HttpManager.request().getAttributes().get("rootResource");
            if( r == null ) {
                r = new RootUniverseResource(universeDao);
                HttpManager.request().getAttributes().put("rootResource", r);
            }
            
            return r;
        }
        Resource rParent = find(path.getParent());
        if (rParent == null) {
            return null;
        }
        if (rParent instanceof CollectionResource) {
            CollectionResource folder = (CollectionResource) rParent;
            return folder.child(path.getName());
        }
        return null;
    }    
}
