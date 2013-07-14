/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */

package io.milton.http.caldav;

import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.http.entity.StringEntity;
import io.milton.http.http11.CustomPostHandler;
import io.milton.resource.SchedulingResponseItem;
import io.milton.resource.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOT USED ... TO BE DELETED
 * 
 * @author brad
 */
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
            System.out.println("Freebusy query: " + iCalText);
            List<SchedulingResponseItem> respItems = outbox.queryFreeBusy(iCalText);

            String xml = schedulingHelper.generateXml(respItems);

            response.setStatus(Response.Status.SC_OK);
            response.setDateHeader(new Date());
            response.setContentTypeHeader("application/xml; charset=\"utf-8\"");
            response.setContentLengthHeader((long)xml.length());
            response.setEntity(new StringEntity(xml));
            // TODO: THIS IS NOT CALLED WITHIN THE STANDARDFILTER? DO WE NEED TO FLUSH HERE AGAIN?
            //response.close();
            System.out.println("FreeBusy response= " + xml);

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean contentTypeIsCalendar(Request r) {
        String s = r.getContentTypeHeader();
        return "text/calendar".equals(s);
    }
}
