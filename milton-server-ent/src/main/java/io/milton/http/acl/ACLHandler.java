/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */

package io.milton.http.acl;

import io.milton.http.Handler;
import io.milton.http.HandlerHelper;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.Response;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.webdav.WebDavResponseHandler;
import io.milton.resource.AccessControlledResource;
import io.milton.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class ACLHandler implements Handler{

    private Logger log = LoggerFactory.getLogger(ACLHandler.class);

    private final WebDavResponseHandler responseHandler;
    private final HandlerHelper handlerHelper;

    public ACLHandler( WebDavResponseHandler responseHandler, HandlerHelper handlerHelper ) {
        this.responseHandler = responseHandler;
        this.handlerHelper = handlerHelper;
    }



    @Override
    public String[] getMethods() {
        return new String[]{Method.ACL.code};
    }

    @Override
    public void process( HttpManager httpManager, Request request, Response response ) throws ConflictException, NotAuthorizedException, BadRequestException {
        response.setStatus( Response.Status.SC_OK );
    }

    @Override
    public boolean isCompatible( Resource res ) {
        return (res instanceof AccessControlledResource);
    }

}
