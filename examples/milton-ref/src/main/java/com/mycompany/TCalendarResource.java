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

import io.milton.common.StreamUtils;
import io.milton.http.values.SupportedCalendarComponentList;
import io.milton.resource.CalendarResource;
import io.milton.resource.ReportableResource;
import io.milton.resource.Resource;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A calendar resource is really just a folder which knows how to create ICal
 * resources.
 *
 * @author brad
 */
public class TCalendarResource extends TFolderResource implements CalendarResource, ReportableResource {

    private static final Logger log = LoggerFactory.getLogger(TCalendarResource.class);

    private String color = "#2952A3";
    
    public TCalendarResource(TFolderResource parent, String name) {
        super(parent, name);
    }

    @Override
    protected Object clone(TFolderResource newParent, String newName) {
        return new TCalendarResource(newParent, newName);
    }

    @Override
    public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException {
        log.debug("createNew: " + contentType);
//        if (contentType.startsWith("text/calendar")) {fredfred
        
            TEvent e = new TEvent(this, newName);
            log.debug("created tevent: " + e.name);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            StreamUtils.readTo(inputStream, bout);
            bout.close();
            String data = bout.toString();
            e.setiCalData(data);
            return e;
//        } else {
//            throw new RuntimeException("eek");
//            //log.debug( "creating a normal resource");
//            //return super.createNew( newName, inputStream, length, contentType );
//        }
    }

	@Override
    public String getCalendarDescription() {
        return "A test calendar";
    }

    @Override
    public String getColor() {
        return color;
    }

    @Override
    public void setColor(String s) {
        this.color = s;
    }

    @Override
    public SupportedCalendarComponentList getSupportedComponentSet() {
        return SupportedCalendarComponentList.asList(ComponentType.VEVENT, ComponentType.VTODO);
    }

}
