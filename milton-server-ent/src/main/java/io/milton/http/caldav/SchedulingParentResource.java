/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.http.caldav;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.principal.CalDavPrincipal;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class SchedulingParentResource extends BaseSchedulingResource {

    private List<Resource> children;
    
    public SchedulingParentResource(CalDavPrincipal principal, SchedulingResourceFactory schedulingResourceFactory) {
        super(principal, schedulingResourceFactory);
    }

    @Override
    public String getName() {
        return schedulingResourceFactory.getSchedulingColName();
    }

    
    
    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        for (Resource r : getChildren()) {
            if (r.getName().equals(childName)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        if( children == null ) {
            children = new ArrayList<Resource>();
            children.add( new SchedulingInboxResource(principal, schedulingResourceFactory));
            children.add( new SchedulingOutboxResource(principal, schedulingResourceFactory));
        }
        return children;
    }
    
}
