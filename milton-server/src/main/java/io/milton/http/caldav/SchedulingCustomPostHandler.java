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

package io.milton.http.caldav;

import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.http.entity.StringEntity;
import io.milton.http.http11.CustomPostHandler;
import io.milton.resource.SchedulingOutboxResource;
import io.milton.resource.SchedulingResponseItem;
import io.milton.resource.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulingCustomPostHandler implements CustomPostHandler {

    private static final Logger log = LoggerFactory.getLogger(SchedulingCustomPostHandler.class);
    private final SchedulingXmlHelper schedulingHelper = new SchedulingXmlHelper();

    @Override
    public boolean supports(Resource resource, Request request) {
        boolean b = resource instanceof SchedulingOutboxResource && contentTypeIsCalendar(request);
        log.trace("supports: " + b);
        return b;
    }

    @Override
    public void process(Resource resource, Request request, Response response) {
        log.trace("process");
        try {
            SchedulingOutboxResource outbox = (SchedulingOutboxResource) resource;
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            IOUtils.copy(request.getInputStream(), bout);
            String iCalText = bout.toString("UTF-8");
            log.trace(iCalText);
            List<SchedulingResponseItem> respItems = outbox.queryFreeBusy(iCalText);

            String xml = schedulingHelper.generateXml(respItems);

            response.setStatus(Response.Status.SC_OK);
            response.setDateHeader(new Date());
            response.setContentTypeHeader("application/xml; charset=\"utf-8\"");
            response.setContentLengthHeader((long)xml.length());
            response.setEntity(new StringEntity(xml));
            // TODO: THIS IS NOT CALLED WITHIN THE STANDARDFILTER? DO WE NEED TO FLUSH HERE AGAIN?
            //response.close();


        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean contentTypeIsCalendar(Request r) {
        String s = r.getContentTypeHeader();
        return "text/calendar".equals(s);
    }
}
