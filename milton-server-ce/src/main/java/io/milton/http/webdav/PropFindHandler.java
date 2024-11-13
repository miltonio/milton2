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

package io.milton.http.webdav;

import io.milton.http.*;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.property.DefaultPropertyAuthoriser;
import io.milton.property.PropertyAuthoriser;
import io.milton.property.PropertyHandler;
import io.milton.resource.PropFindableResource;
import io.milton.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author brad
 */
public class PropFindHandler implements ExistingEntityHandler, PropertyHandler {

    private static final Logger log = LoggerFactory.getLogger(PropFindHandler.class);
    private final ResourceHandlerHelper resourceHandlerHelper;
    private final PropFindRequestFieldParser requestFieldParser;
    private final WebDavResponseHandler responseHandler;
    private final PropFindPropertyBuilder propertyBuilder;
    private final PropertyAuthoriser permissionService = new DefaultPropertyAuthoriser();

//    /**
//     *
//     * @param resourceHandlerHelper
//     * @param resourceTypeHelper
//     * @param responseHandler
//     */
//    public PropFindHandler( ResourceHandlerHelper resourceHandlerHelper, ResourceTypeHelper resourceTypeHelper, WebDavResponseHandler responseHandler, List<PropertySource> propertySources ) {
//        this.resourceHandlerHelper = resourceHandlerHelper;
//
//        DefaultPropFindRequestFieldParser defaultFieldParse = new DefaultPropFindRequestFieldParser();
//        this.requestFieldParser = new MsPropFindRequestFieldParser( defaultFieldParse ); // use MS decorator for windows support
//        this.responseHandler = responseHandler;
//
//        this.propertyBuilder = new PropFindPropertyBuilder( propertySources );
//    }

    /**
     * @param resourceHandlerHelper
     * @param requestFieldParser
     * @param responseHandler
     * @param propertyBuilder
     */
    public PropFindHandler(ResourceHandlerHelper resourceHandlerHelper, PropFindRequestFieldParser requestFieldParser, WebDavResponseHandler responseHandler, PropFindPropertyBuilder propertyBuilder) {
        this.resourceHandlerHelper = resourceHandlerHelper;
        this.requestFieldParser = requestFieldParser;
        if (requestFieldParser == null) {
            throw new NullPointerException("Must provide a PropFindRequestFieldParser");
        }
        this.responseHandler = responseHandler;
        this.propertyBuilder = propertyBuilder;
    }

    @Override
    public String[] getMethods() {
        return new String[]{Method.PROPFIND.code};
    }

    @Override
    public boolean isCompatible(Resource handler) {
        return (handler instanceof PropFindableResource);
    }

    @Override
    public void process(HttpManager httpManager, Request request, Response response) throws ConflictException, NotAuthorizedException, BadRequestException {
        resourceHandlerHelper.process(httpManager, request, response, this);
    }

    @Override
    public void processResource(HttpManager manager, Request request, Response response, Resource r) throws NotAuthorizedException, ConflictException, BadRequestException {
        manager.onGet(request, response, r, request.getParams());
        resourceHandlerHelper.processResource(manager, request, response, r, this, true, request.getParams(), null);
    }

    @Override
    public void processExistingResource(HttpManager manager, Request request, Response response, Resource resource) throws NotAuthorizedException, BadRequestException, ConflictException {
        log.trace("processExistingResource");
        PropFindableResource pfr = (PropFindableResource) resource;
        int depth = request.getDepthHeader();
        response.setStatus(Response.Status.SC_MULTI_STATUS);
        response.setContentTypeHeader(Response.APPLICATION_XML);
//        response.setContentTypeHeader( Response.XML );
        PropertiesRequest parseResult;
        try {
            parseResult = requestFieldParser.getRequestedFields(request.getInputStream());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        String url = request.getAbsoluteUrl();

        // Check that the current user has permission to read requested fields
        Set<QName> allFields = getAllFields(parseResult, pfr);
        Set<PropertyAuthoriser.CheckResult> errorFields = permissionService.checkPermissions(request, request.getMethod(), PropertyAuthoriser.PropertyPermission.READ, allFields, resource);
        if (errorFields != null && errorFields.size() > 0) {
            if (log.isTraceEnabled()) {
                log.trace("permissionService denied access: " + permissionService.getClass().getCanonicalName());
            }
            responseHandler.respondUnauthorised(resource, response, request);
        } else {
            if (log.isTraceEnabled()) {
                log.trace("Listing requested propfind properties ---");
                for (PropertiesRequest.Property p : parseResult.getProperties()) {
                    log.trace(p.getName().toString());
                }
                log.trace("---");
            }
            List<PropFindResponse> propFindResponses;
            try {
                propFindResponses = propertyBuilder.buildProperties(pfr, depth, parseResult, url);
            } catch (URISyntaxException ex) {
                log.error("Exception parsing url. request class: " + request.getClass() + ". Please check the client application is usign percentage encoding (see http://en.wikipedia.org/wiki/Percent-encoding)");
                throw new RuntimeException("Exception parsing url, indicating the requested URL is not correctly encoded. Please check the client application. Requested url is: " + url, ex);
            }
            if (log.isTraceEnabled()) {
                log.trace("responses: " + propFindResponses.size());
            }
            responseHandler.respondPropFind(propFindResponses, response, request, pfr);
        }
    }

    private Set<QName> getAllFields(PropertiesRequest parseResult, PropFindableResource resource) throws NotAuthorizedException, BadRequestException {
        Set<QName> set = new HashSet<>();
        if (parseResult.isAllProp() || parseResult.isPropname()) {
            set.addAll(propertyBuilder.findAllProps(resource));
        } else {
            set.addAll(parseResult.getNames());
        }
        return set;
    }

    @Override
    public PropertyAuthoriser getPermissionService() {
        return permissionService;
    }
}
