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

import io.milton.http.ExistingEntityHandler;
import io.milton.resource.Resource;
import io.milton.http.ResourceHandlerHelper;
import io.milton.http.HttpManager;
import io.milton.http.AuthenticationService.AuthStatus;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.webdav.PropPatchRequestParser.ParseResult;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.milton.http.Request.Method;
import io.milton.http.Response.Status;
import io.milton.common.ReadingException;
import io.milton.common.WritingException;
import io.milton.property.PropertyHandler;
import io.milton.property.PropertyAuthoriser;
import io.milton.event.PropPatchEvent;
import io.milton.http.Request;
import io.milton.http.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import org.apache.commons.io.IOUtils;

/**
 * Example request (from ms office)
 *
 * PROPPATCH /Documents/test.docx HTTP/1.1
content-length: 371
cache-control: no-cache
connection: Keep-Alive
host: milton:8080
user-agent: Microsoft-WebDAV-MiniRedir/6.0.6001
pragma: no-cache
translate: f
if: (<opaquelocktoken:900f718e-801c-4152-ae8e-f9395fe45d71>)
content-type: text/xml; charset="utf-8"
<?xml version="1.0" encoding="utf-8" ?>
 * <D:propertyupdate xmlns:D="DAV:" xmlns:Z="urn:schemas-microsoft-com:">
 *  <D:set>
 *  <D:prop>
 *  <Z:Win32LastAccessTime>Wed, 10 Dec 2008 21:55:22 GMT</Z:Win32LastAccessTime>
 *  <Z:Win32LastModifiedTime>Wed, 10 Dec 2008 21:55:22 GMT</Z:Win32LastModifiedTime>
 *  <Z:Win32FileAttributes>00000020</Z:Win32FileAttributes>
 * </D:prop>
 * </D:set>
 * </D:propertyupdate>
 *
 *
 * And another example request (from spec)
 *
 *    <?xml version="1.0" encoding="utf-8" ?>
<D:propertyupdate xmlns:D="DAV:"
xmlns:Z="http://www.w3.com/standards/z39.50/">
<D:set>
<D:prop>
<Z:authors>
<Z:Author>Jim Whitehead</Z:Author>
<Z:Author>Roy Fielding</Z:Author>
</Z:authors>
</D:prop>
</D:set>
<D:remove>
<D:prop><Z:Copyright-Owner/></D:prop>
</D:remove>
</D:propertyupdate>

 *
 *
 * Here is an example response (from the spec)
 *
 *    HTTP/1.1 207 Multi-Status
Content-Type: text/xml; charset="utf-8"
Content-Length: xxxx

<?xml version="1.0" encoding="utf-8" ?>
<D:multistatus xmlns:D="DAV:" xmlns:Z="http://www.w3.com/standards/z39.50">
<D:response>
<D:href>http://www.foo.com/bar.html</D:href>
<D:propstat>
<D:prop><Z:Authors/></D:prop>
<D:status>HTTP/1.1 424 Failed Dependency</D:status>
</D:propstat>
<D:propstat>
<D:prop><Z:Copyright-Owner/></D:prop>
<D:status>HTTP/1.1 409 Conflict</D:status>
</D:propstat>
<D:responsedescription> Copyright Owner can not be deleted or altered.</D:responsedescription>
</D:response>
</D:multistatus>

 *
 *
 * @author brad
 */
public class PropPatchHandler implements ExistingEntityHandler, PropertyHandler {

    private final static Logger log = LoggerFactory.getLogger( PropPatchHandler.class );
    private final ResourceHandlerHelper resourceHandlerHelper;
    private final PropPatchRequestParser requestParser;
    private final PropPatchSetter patchSetter;
    private final WebDavResponseHandler responseHandler;
    private final PropertyAuthoriser permissionService;

    public PropPatchHandler( ResourceHandlerHelper resourceHandlerHelper, PropPatchRequestParser requestParser, PropPatchSetter patchSetter, WebDavResponseHandler responseHandler, PropertyAuthoriser permissionService ) {
        this.resourceHandlerHelper = resourceHandlerHelper;
        this.requestParser = requestParser;
        this.patchSetter = patchSetter;
        this.responseHandler = responseHandler;
		this.permissionService = permissionService;
    }

	@Override
    public String[] getMethods() {
        return new String[]{Method.PROPPATCH.code};
    }

	@Override
    public boolean isCompatible( Resource r ) {
        return patchSetter.supports( r );
    }

	@Override
    public void process( HttpManager httpManager, Request request, Response response ) throws ConflictException, NotAuthorizedException, BadRequestException {
        resourceHandlerHelper.process( httpManager, request, response, this );
    }

	@Override
    public void processResource( HttpManager manager, Request request, Response response, Resource resource ) throws NotAuthorizedException, ConflictException, BadRequestException {
        long t = System.currentTimeMillis();
        try {

            manager.onProcessResourceStart( request, response, resource );

            if( resourceHandlerHelper.isNotCompatible( resource, request.getMethod() ) || !isCompatible( resource ) ) {
                log.debug( "resource not compatible. Resource class: " + resource.getClass() + " handler: " + getClass() );
                responseHandler.respondMethodNotImplemented( resource, response, request );
                return;
            }

            AuthStatus authStatus = resourceHandlerHelper.checkAuthentication( manager, resource, request );
            if( authStatus != null && authStatus.loginFailed ) {
                log.debug( "authentication failed. respond with: " + responseHandler.getClass().getCanonicalName() + " resource: " + resource.getClass().getCanonicalName() );
                responseHandler.respondUnauthorised( resource, response, request );
                return;
            }

            if( request.getMethod().isWrite ) {
                if( resourceHandlerHelper.isLockedOut( request, resource ) ) {
                    response.setStatus( Status.SC_LOCKED ); // replace with responsehandler method
                    return;
                }
            }

            processExistingResource( manager, request, response, resource );
        } finally {
            t = System.currentTimeMillis() - t;
            manager.onProcessResourceFinish( request, response, resource, t );
        }
    }

	@Override
    public void processExistingResource( HttpManager manager, Request request, Response response, Resource resource ) throws NotAuthorizedException, BadRequestException, ConflictException {
        // todo: check if token header
        try {
            PropFindResponse resp = doPropPatch( request, resource);

            manager.getEventManager().fireEvent( new PropPatchEvent( resource, resp ) );
            List<PropFindResponse> responses = new ArrayList<PropFindResponse>();
            responses.add( resp );
            responseHandler.respondPropFind( responses, response, request, resource );
        } catch( NotAuthorizedException e ) {
            responseHandler.respondUnauthorised( resource, response, request );
        } catch( WritingException ex ) {
            throw new RuntimeException( ex );
        } catch( ReadingException ex ) {
            throw new RuntimeException( ex );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
    }

    public PropFindResponse doPropPatch(Request request, Resource resource) throws NotAuthorizedException, IOException, BadRequestException {
        InputStream in = request.getInputStream();
        ParseResult parseResult = requestParser.getRequestedFields(in);
        // Check that the current user has permission to write requested fields
        Set<QName> allFields = getAllFields(parseResult);
        if (log.isTraceEnabled()) {
            log.trace("check permissions with: " + permissionService.getClass().getCanonicalName());
        }
        Set<PropertyAuthoriser.CheckResult> errorFields = permissionService.checkPermissions(request, request.getMethod(), PropertyAuthoriser.PropertyPermission.WRITE, allFields, resource);
        if (errorFields != null && errorFields.size() > 0) {
            throw new NotAuthorizedException(resource);
        }
        String href = request.getAbsoluteUrl();
		href = DefaultPropFindPropertyBuilder.fixUrlForWindows(href);
        PropFindResponse resp = patchSetter.setProperties(href, parseResult, resource);
        return resp;
    }

    private Set<QName> getAllFields( ParseResult parseResult ) {
        Set<QName> set = new HashSet<QName>();
        if( parseResult.getFieldsToRemove() != null ) {
            set.addAll( parseResult.getFieldsToRemove() );
        }
        if( parseResult.getFieldsToSet() != null ) {
            set.addAll( parseResult.getFieldsToSet().keySet() );
        }
        return set;
    }

	@Override
    public PropertyAuthoriser getPermissionService() {
        return permissionService;
    }

    public static class Field {

        public final String name;
        String namespaceUri;

        public Field( String name ) {
            this.name = name;
        }

        public void setNamespaceUri( String namespaceUri ) {
            this.namespaceUri = namespaceUri;
        }

        public String getNamespaceUri() {
            return namespaceUri;
        }
    }

    public static class SetField extends Field {

        public final String value;

        public SetField( String name, String value ) {
            super( name );
            this.value = value;
        }
    }

    public static class Fields implements Iterable<Field> {

        /**
         * fields to remove
         */
        public final List<Field> removeFields = new ArrayList<Field>();
        /**
         * fields to set to a value
         */
        public final List<SetField> setFields = new ArrayList<PropPatchHandler.SetField>();

        private int size() {
            return removeFields.size() + setFields.size();
        }

		@Override
        public Iterator<Field> iterator() {
            List<Field> list = new ArrayList<Field>( removeFields );
            list.addAll( setFields );
            return list.iterator();
        }
    }
}

