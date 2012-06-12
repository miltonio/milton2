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

import io.milton.common.StreamUtils;
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
    protected Object clone(TFolderResource newParent) {
        return new TCalendarResource(newParent, name);
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

}
