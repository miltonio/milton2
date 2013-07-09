/*
 * Copyright 2012 McEvoy Software Ltd.
 
 */
package io.milton.http.caldav;

import io.milton.http.*;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.webdav.MkColHandler;
import io.milton.http.webdav.PropFindResponse;
import io.milton.http.webdav.PropPatchHandler;
import io.milton.http.webdav.PropPatchParseResult;
import io.milton.http.webdav.PropPatchRequestParser;
import io.milton.http.webdav.PropPatchSetter;
import io.milton.resource.MakeCalendarResource;
import io.milton.resource.CollectionResource;
import io.milton.resource.ExtMakeCalendarResource;
import io.milton.resource.MakeCollectionableResource;
import io.milton.resource.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class MkCalendarHandler implements Handler, MkColHandler.CollectionResourceCreator {

    private static final Logger log = LoggerFactory.getLogger(MkCalendarHandler.class);
    private final MkColHandler mkColHandler;
    private final PropPatchHandler propPatchHandler;

    public MkCalendarHandler(MkColHandler mkColHandler, PropPatchHandler propPatchHandler) {
        this.mkColHandler = mkColHandler;
        this.propPatchHandler = propPatchHandler;
    }

    @Override
    public String[] getMethods() {
        return new String[]{Method.MKCALENDAR.code};
    }

    @Override
    public boolean isCompatible(Resource handler) {
        return (handler instanceof MakeCalendarResource);
    }

    @Override
    public void process(HttpManager manager, Request request, Response response) throws ConflictException, NotAuthorizedException, BadRequestException {
        log.info("process");
        try {
            mkColHandler.process(manager, request, response, this);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public CollectionResource createResource(MakeCollectionableResource existingCol, String newName, Request request) throws ConflictException, NotAuthorizedException, BadRequestException, IOException {
        log.info("createResource");
        if (existingCol instanceof ExtMakeCalendarResource) {
            // This way does the create and set properties in a single operation
            final ExtMakeCalendarResource col = (ExtMakeCalendarResource) existingCol;
            final List<CollectionResource> theNewCol = new ArrayList<CollectionResource>();
            PropFindResponse resp = propPatchHandler.doPropPatch(request, null, new PropPatchSetter() {
                @Override
                public PropFindResponse setProperties(String href, PropPatchParseResult parseResult, Resource r) throws NotAuthorizedException, BadRequestException, ConflictException {
                    CollectionResource newCal = col.createCalendar(href, parseResult.getFieldsToSet());
                    theNewCol.add(newCal);
                    PropFindResponse resp = new PropFindResponse(href, Collections.EMPTY_MAP, Collections.EMPTY_MAP);
                    return resp;
                }

                @Override
                public boolean supports(Resource r) {
                    return true;
                }
            });
            return theNewCol.get(0);
        } else {
            // This is the old way of doing it, create a collection then set properties
            MakeCalendarResource mkcal = (MakeCalendarResource) existingCol;
            CollectionResource newCal = mkcal.createCalendar(newName);
            log.info("do proppatch on new calendar collection resource");
            propPatchHandler.doPropPatch(request, newCal);
            return newCal;
        }

    }
}
