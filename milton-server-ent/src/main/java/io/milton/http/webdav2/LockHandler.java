/*
 * Copyright 2012 McEvoy Software Ltd.
 */
package io.milton.http.webdav2;

import io.milton.http.HttpManager;
import io.milton.resource.Resource;
import io.milton.resource.LockingCollectionResource;
import io.milton.resource.LockableResource;
import io.milton.http.LockResult;
import io.milton.http.ResourceHandler;
import io.milton.http.LockToken;
import io.milton.http.HandlerHelper;
import io.milton.http.LockTimeout;
import io.milton.http.LockInfo;
import io.milton.common.Path;
import io.milton.http.Request.Method;
import io.milton.http.Response.Status;
import io.milton.http.*;
import io.milton.http.exceptions.*;
import io.milton.http.webdav.WebDavResponseHandler;
import io.milton.webdav.utils.LockUtils;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Note that this is both a new entity handler and an existing entity handler
 *
 * @author brad
 */
public class LockHandler implements ResourceHandler {

    private static final Logger log = LoggerFactory.getLogger(LockHandler.class);
            
    private final WebDavResponseHandler responseHandler;
    private final HandlerHelper handlerHelper;

    public LockHandler(WebDavResponseHandler responseHandler, HandlerHelper handlerHelper) {
        this.responseHandler = responseHandler;
        this.handlerHelper = handlerHelper;
        LockUtils.init(); 
    }

    @Override
    public void processResource(HttpManager manager, Request request, Response response, Resource r) throws NotAuthorizedException, ConflictException, BadRequestException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getMethods() {
        return new String[]{Method.LOCK.code};
    }

    @Override
    public void process(HttpManager manager, Request request, Response response) throws NotAuthorizedException, BadRequestException {
        if (!handlerHelper.checkExpects(responseHandler, request, response)) {
            return;
        }

        String host = request.getHostHeader();
        String url = HttpManager.decodeUrl(request.getAbsolutePath());

        // Find a resource if it exists
        Resource r = manager.getResourceFactory().getResource(host, url);
        if (r != null) {
            log.debug("locking existing resource: " + r.getName());
            processExistingResource(manager, request, response, r);
        } else {
            log.debug("lock target doesnt exist, attempting lock null..");
            processNonExistingResource(manager, request, response, host, url);
        }
    }

    protected void processExistingResource(HttpManager manager, Request request, Response response, Resource resource) throws NotAuthorizedException {
        if (handlerHelper.isNotCompatible(resource, request.getMethod()) || !isCompatible(resource)) {
            responseHandler.respondMethodNotImplemented(resource, response, request);
            return;
        }
        if (!handlerHelper.checkAuthorisation(manager, resource, request)) {
            responseHandler.respondUnauthorised(resource, response, request);
            return;
        }

        handlerHelper.checkExpects(responseHandler, request, response);

        LockableResource r = (LockableResource) resource;
        LockTimeout timeout = LockTimeout.parseTimeout(request);
        String ifHeader = request.getIfHeader();
        response.setContentTypeHeader(Response.XML);
        if (ifHeader == null || ifHeader.length() == 0) {
            processNewLock(manager, request, response, r, timeout);
        } else {
            processRefresh(manager, request, response, r, timeout, ifHeader);
        }
    }

    /**
     * (from the spec) 7.4 Write Locks and Null Resources
     *
     * It is possible to assert a write lock on a null resource in order to lock
     * the name.
     *
     * A write locked null resource, referred to as a lock-null resource, MUST
     * respond with a 404 (Not Found) or 405 (Method Not Allowed) to any
     * HTTP/1.1 or DAV methods except for PUT, MKCOL, OPTIONS, PROPFIND, LOCK,
     * and UNLOCK. A lock-null resource MUST appear as a member of its parent
     * collection. Additionally the lock-null resource MUST have defined on it
     * all mandatory DAV properties. Most of these properties, such as all the
     * get* properties, will have no value as a lock-null resource does not
     * support the GET method. Lock-Null resources MUST have defined values for
     * lockdiscovery and supportedlock properties.
     *
     * Until a method such as PUT or MKCOL is successfully executed on the
     * lock-null resource the resource MUST stay in the lock-null state.
     * However, once a PUT or MKCOL is successfully executed on a lock-null
     * resource the resource ceases to be in the lock-null state.
     *
     * If the resource is unlocked, for any reason, without a PUT, MKCOL, or
     * similar method having been successfully executed upon it then the
     * resource MUST return to the null state.
     *
     *
     * @param manager
     * @param request
     * @param response
     * @param host
     * @param url
     */
    private void processNonExistingResource(HttpManager manager, Request request, Response response, String host, String url) throws NotAuthorizedException, BadRequestException {
        String name;

        Path parentPath = Path.path(url);
        name = parentPath.getName();
        parentPath = parentPath.getParent();
        url = parentPath.toString();

        Resource r = manager.getResourceFactory().getResource(host, url);
        if (r != null) {
            if (!handlerHelper.checkAuthorisation(manager, r, request)) {
                responseHandler.respondUnauthorised(r, response, request);
                return;
            } else {
                processCreateAndLock(manager, request, response, r, name);
            }
        } else {
            log.debug("couldnt find parent to execute lock-null, returning not found");
            //respondNotFound(response,request);
            response.setStatus(Status.SC_CONFLICT);

        }
    }

    private void processCreateAndLock(HttpManager manager, Request request, Response response, Resource parentResource, String name) throws NotAuthorizedException {
        if (parentResource instanceof LockingCollectionResource) {
            log.debug("parent supports lock-null. doing createAndLock");
            LockingCollectionResource lockingParent = (LockingCollectionResource) parentResource;
            LockTimeout timeout = LockTimeout.parseTimeout(request);
            response.setContentTypeHeader(Response.XML);

            LockInfo lockInfo;
            try {
                lockInfo = LockInfoSaxHandler.parseLockInfo(request);
            } catch (SAXException ex) {
                throw new RuntimeException("Exception reading request body", ex);
            } catch (IOException ex) {
                throw new RuntimeException("Exception reading request body", ex);
            }

            // TODO: this should be refactored to return a LockResult as for existing entities

            log.debug("Creating lock on unmapped resource: " + name);
            LockToken tok = lockingParent.createAndLock(name, timeout, lockInfo);
            if (tok == null) {
                throw new RuntimeException("createAndLock returned null, from resource of type: " + lockingParent.getClass().getCanonicalName());
            }
            response.setStatus(Status.SC_CREATED);
            response.setLockTokenHeader("<opaquelocktoken:" + tok.tokenId + ">");  // spec says to set response header. See 8.10.1
            LockUtils.respondLocked(tok, request, response);

        } else {
            log.debug("parent does not support lock-null, respondong method not allowed");
            responseHandler.respondMethodNotImplemented(parentResource, response, request);
        }
    }

    @Override
    public boolean isCompatible(Resource handler) {
        return handler instanceof LockableResource;
    }

    protected void processNewLock(HttpManager milton, Request request, Response response, LockableResource r, LockTimeout timeout) throws NotAuthorizedException {
        LockInfo lockInfo;
        try {
            lockInfo = LockInfoSaxHandler.parseLockInfo(request);
        } catch (SAXException ex) {
            throw new RuntimeException("Exception reading request body", ex);
        } catch (IOException ex) {
            throw new RuntimeException("Exception reading request body", ex);
        }

        if (handlerHelper.isLockedOut(request, r)) {
            this.responseHandler.respondLocked(request, response, r);
            return;
        }

        log.debug("locking: " + r.getName());
        LockResult result;
        try {
            result = r.lock(timeout, lockInfo);
        } catch (PreConditionFailedException ex) {
            responseHandler.respondPreconditionFailed(request, response, r);
            return;
        } catch (LockedException ex) {
            responseHandler.respondLocked(request, response, r);
            return;
        }

        if (result.isSuccessful()) {
            LockToken tok = result.getLockToken();
            log.debug("..locked ok: " + tok.tokenId);
            response.setLockTokenHeader("<opaquelocktoken:" + tok.tokenId + ">");  // spec says to set response header. See 8.10.1
            LockUtils.respondLocked(tok, request, response);
        } else {
            LockUtils.respondLockFailure(result, request, response);
        }
    }

    protected void processRefresh(HttpManager milton, Request request, Response response, LockableResource r, LockTimeout timeout, String ifHeader) throws NotAuthorizedException {
        String token = LockUtils.parse(ifHeader);
        log.debug("refreshing lock: " + token);
        LockResult result;
        try {
            result = r.refreshLock(token);
        } catch (PreConditionFailedException ex) {
            responseHandler.respondPreconditionFailed(request, response, r);
            return;
        }
        if (result.isSuccessful()) {
            LockToken tok = result.getLockToken();
            LockUtils.respondLocked(tok, request, response);
        } else {
            LockUtils.respondLockFailure(result, request, response);
        }
    }


}
