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

import io.milton.common.Path;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.principal.CalDavPrincipal;
import io.milton.resource.Resource;

/**
 * Adds scheduling inbox and outbox resources to the resource tree for each
 * principal
 * 
 * This class is intended to wrap the real ResourceFactory, such that calls
 * to wrapped.getResource("..","/users/brad/") will return a CalDavPrincipal
 * representing brad
 * 
 * By default, when no scheduling resource is found this will just return the
 * resource from the wrapped ResourceFactory, ie the decorator pattern
 * 
 * But if you want to use this with a MultiResourceFactory then set decorator to
 * false, then it will simply return null
 *
 * @author brad
 */
public class SchedulingResourceFactory implements ResourceFactory {

    private final ResourceFactory wrapped;
    private final CalendarSearchService calendarSearchService;
    
    private String schedulingColName = "scheduling";
    private String inboxName = "inbox";
    private String outBoxName = "outbox";
    private boolean decorator = true;

    public SchedulingResourceFactory(ResourceFactory wrapped, CalendarSearchService calendarSearchService) {
        this.wrapped = wrapped;
        this.calendarSearchService = calendarSearchService;
    }

    @Override
    public Resource getResource(String host, String sPath) throws NotAuthorizedException, BadRequestException {
        Path path = Path.path(sPath);
        if (path.getName().equals(schedulingColName)) {
            SchedulingParentResource schedulingParentResource = getSchedulingResource(host, path);
            if( schedulingParentResource != null ) {
                return schedulingParentResource;
            }
        } else if (path.getParent().getName().equals(schedulingColName)) {
            SchedulingParentResource schedulingParentResource = getSchedulingResource(host, path.getParent());
            if( schedulingParentResource != null ) {
                return schedulingParentResource.child(path.getName());
            }
        }
        if( decorator ) {
            return wrapped.getResource(host, sPath);
        } else {
            return null;
        }
    }

    private SchedulingParentResource getSchedulingResource(String host, Path path) throws NotAuthorizedException, BadRequestException {
        // We expect the parent of the scheduling resource to be the user
        Resource r = wrapped.getResource(host, path.getParent().toString());
        if (r instanceof CalDavPrincipal) {
            CalDavPrincipal p = (CalDavPrincipal) r;
            return new SchedulingParentResource(p, this);
        }
        return null;
    }

    public boolean isDecorator() {
        return decorator;
    }

    public void setDecorator(boolean decorator) {
        this.decorator = decorator;
    }

    public String getInboxName() {
        return inboxName;
    }

    public String getOutboxName() {
        return outBoxName;
    }

    public String getSchedulingColName() {
        return schedulingColName;
    }

    public ResourceFactory getWrapped() {
        return wrapped;
    }

    public CalendarSearchService getCalendarSearchService() {
        return calendarSearchService;
    }
 
    
}
