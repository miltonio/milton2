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

package io.milton.http.webdav;

import io.milton.http.ExistingEntityHandler;
import io.milton.resource.Resource;
import io.milton.http.HttpManager;
import io.milton.http.Response;
import io.milton.resource.PropFindableResource;
import io.milton.http.Request;
import io.milton.http.ResourceHandlerHelper;
import io.milton.property.PropertySource;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.milton.http.Request.Method;
import io.milton.property.DefaultPropertyAuthoriser;
import io.milton.property.PropertyHandler;
import io.milton.property.PropertyAuthoriser;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author brad
 */
public class PropFindHandler implements ExistingEntityHandler, PropertyHandler {

    private static final Logger log = LoggerFactory.getLogger( PropFindHandler.class );
    private final ResourceHandlerHelper resourceHandlerHelper;
    private final PropFindRequestFieldParser requestFieldParser;
    private final WebDavResponseHandler responseHandler;
    private final PropFindPropertyBuilder propertyBuilder;
    private final PropertyAuthoriser permissionService = new DefaultPropertyAuthoriser();

    /**
     * 
     * @param resourceHandlerHelper
     * @param resourceTypeHelper
     * @param responseHandler
     */
    public PropFindHandler( ResourceHandlerHelper resourceHandlerHelper, ResourceTypeHelper resourceTypeHelper, WebDavResponseHandler responseHandler, List<PropertySource> propertySources ) {
        this.resourceHandlerHelper = resourceHandlerHelper;

        DefaultPropFindRequestFieldParser defaultFieldParse = new DefaultPropFindRequestFieldParser();
        this.requestFieldParser = new MsPropFindRequestFieldParser( defaultFieldParse ); // use MS decorator for windows support
        this.responseHandler = responseHandler;

        this.propertyBuilder = new PropFindPropertyBuilder( propertySources );
    }

    /**
     *
     * @param resourceHandlerHelper
     * @param requestFieldParser
     * @param responseHandler
     * @param propertyBuilder
     */
    public PropFindHandler( ResourceHandlerHelper resourceHandlerHelper, PropFindRequestFieldParser requestFieldParser, WebDavResponseHandler responseHandler, PropFindPropertyBuilder propertyBuilder ) {
        this.resourceHandlerHelper = resourceHandlerHelper;
        this.requestFieldParser = requestFieldParser;
        this.responseHandler = responseHandler;
        this.propertyBuilder = propertyBuilder;
    }

	@Override
    public String[] getMethods() {
        return new String[]{Method.PROPFIND.code};
    }

    @Override
    public boolean isCompatible( Resource handler ) {
        return ( handler instanceof PropFindableResource );
    }

	@Override
    public void process( HttpManager httpManager, Request request, Response response ) throws ConflictException, NotAuthorizedException, BadRequestException {
        resourceHandlerHelper.process( httpManager, request, response, this );
    }

	@Override
    public void processResource( HttpManager manager, Request request, Response response, Resource r ) throws NotAuthorizedException, ConflictException, BadRequestException {
        manager.onGet( request, response, r, request.getParams() );
        resourceHandlerHelper.processResource( manager, request, response, r, this, true, request.getParams(), null );
    }

	@Override
    public void processExistingResource( HttpManager manager, Request request, Response response, Resource resource ) throws NotAuthorizedException, BadRequestException, ConflictException {
        log.trace( "processExistingResource" );
        PropFindableResource pfr = (PropFindableResource) resource;
        int depth = request.getDepthHeader();
        response.setStatus( Response.Status.SC_MULTI_STATUS );
        response.setContentTypeHeader( Response.XML );
        PropertiesRequest parseResult;
        try {
            parseResult = requestFieldParser.getRequestedFields( request.getInputStream() );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
        String url = request.getAbsoluteUrl();

        // Check that the current user has permission to write requested fields
        Set<QName> allFields = getAllFields( parseResult, pfr );
        Set<PropertyAuthoriser.CheckResult> errorFields = permissionService.checkPermissions( request, request.getMethod(), PropertyAuthoriser.PropertyPermission.READ, allFields, resource );
        if( errorFields != null && errorFields.size() > 0 ) {
            if( log.isTraceEnabled() ) {
                log.trace( "permissionService denied access: " + permissionService.getClass().getCanonicalName() );
            }
            responseHandler.respondUnauthorised( resource, response, request );
        } else {
            List<PropFindResponse> propFindResponses;
			try {
				propFindResponses = propertyBuilder.buildProperties( pfr, depth, parseResult, url );
			} catch (URISyntaxException ex) {
				log.error("Exception parsing url. request class: " + request.getClass() + ". Please check the client application is usign percentage encoding (see http://en.wikipedia.org/wiki/Percent-encoding)");
				throw new RuntimeException("Exception parsing url, indicating the requested URL is not correctly encoded. Please check the client application. Requested url is: " + url, ex);
			}
            if( log.isTraceEnabled() ) {
                log.trace( "responses: " + propFindResponses.size() );
            }
            responseHandler.respondPropFind( propFindResponses, response, request, pfr );
        }
    }

    private Set<QName> getAllFields( PropertiesRequest parseResult, PropFindableResource resource ) {
        Set<QName> set = new HashSet<QName>();
        if( parseResult.isAllProp() ) {
            set.addAll( propertyBuilder.findAllProps( resource ) );
        } else {
            set.addAll( parseResult.getNames() );
        }
        return set;
    }

	@Override
    public PropertyAuthoriser getPermissionService() {
        return permissionService;
    }
}
