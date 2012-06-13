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

import io.milton.resource.GetableResource;
import io.milton.http.HrefStatus;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.resource.Resource;
import io.milton.http.Response;
import io.milton.http.Response.Status;
import io.milton.common.Utils;
import io.milton.http.entity.ByteArrayEntity;
import io.milton.http.entity.MultiStatusEntity;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.http.http11.Bufferable;
import io.milton.http.http11.DefaultHttp11ResponseHandler.BUFFERING;
import io.milton.http.http11.Http11ResponseHandler;
import io.milton.http.quota.StorageChecker.StorageErrorReason;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class DefaultWebDavResponseHandler implements WebDavResponseHandler, Bufferable {

    private static final Logger log = LoggerFactory.getLogger( DefaultWebDavResponseHandler.class );
    protected final Http11ResponseHandler wrapped;
    protected final ResourceTypeHelper resourceTypeHelper;
    protected final PropFindXmlGenerator propFindXmlGenerator;


    public DefaultWebDavResponseHandler( Http11ResponseHandler wrapped, ResourceTypeHelper resourceTypeHelper, PropFindXmlGenerator propFindXmlGenerator ) {
        this.wrapped = wrapped;
        this.resourceTypeHelper = resourceTypeHelper;
        this.propFindXmlGenerator = propFindXmlGenerator;
    }

	@Override
    public String generateEtag( Resource r ) {
        return wrapped.generateEtag( r );
    }

	@Override
    public void respondWithOptions( Resource resource, Response response, Request request, List<String> methodsAllowed ) {
        wrapped.respondWithOptions( resource, response, request, methodsAllowed );
        List<String> supportedLevels = resourceTypeHelper.getSupportedLevels( resource );
        String s = Utils.toCsv( supportedLevels );
        response.setDavHeader( s );
        response.setNonStandardHeader( "MS-Author-Via", "DAV" );
    }

	@Override
    public void responseMultiStatus( Resource resource, Response response, Request request, List<HrefStatus> statii ) {
        response.setStatus( Response.Status.SC_MULTI_STATUS );
        response.setDateHeader( new Date() );
        response.setContentTypeHeader( Response.XML );
        List<String> supportedLevels = resourceTypeHelper.getSupportedLevels( resource );
        String s = Utils.toCsv( supportedLevels );
        response.setDavHeader( s );
        response.setEntity(new MultiStatusEntity(statii));
    }

	@Override
    public void respondNoContent( Resource resource, Response response, Request request ) {
        wrapped.respondNoContent( resource, response, request );
    }

	@Override
    public void respondContent( Resource resource, Response response, Request request, Map<String, String> params ) throws NotAuthorizedException, BadRequestException, NotFoundException {
        wrapped.respondContent( resource, response, request, params );
    }

	@Override
    public void respondPartialContent( GetableResource resource, Response response, Request request, Map<String, String> params, Range range ) throws NotAuthorizedException, BadRequestException, NotFoundException {
        wrapped.respondPartialContent( resource, response, request, params, range );
    }

	@Override
    public void respondCreated( Resource resource, Response response, Request request ) {
        wrapped.respondCreated( resource, response, request );
    }

	@Override
    public void respondUnauthorised( Resource resource, Response response, Request request ) {
        wrapped.respondUnauthorised( resource, response, request );
    }

	@Override
    public void respondMethodNotImplemented( Resource resource, Response response, Request request ) {
        wrapped.respondMethodNotImplemented( resource, response, request );
    }

	@Override
    public void respondMethodNotAllowed( Resource res, Response response, Request request ) {
        wrapped.respondMethodNotAllowed( res, response, request );
    }

	@Override
    public void respondConflict( Resource resource, Response response, Request request, String message ) {
        wrapped.respondConflict( resource, response, request, message );
    }

	@Override
    public void respondRedirect( Response response, Request request, String redirectUrl ) {
        wrapped.respondRedirect( response, request, redirectUrl );
    }

	@Override
    public void respondNotModified( GetableResource resource, Response response, Request request ) {
        if( log.isTraceEnabled() ) {
            log.trace( "respondNotModified: " + wrapped.getClass().getCanonicalName() );
        }
        wrapped.respondNotModified( resource, response, request );
    }

	@Override
    public void respondNotFound( Response response, Request request ) {
        wrapped.respondNotFound( response, request );
    }

	@Override
    public void respondHead( Resource resource, Response response, Request request ) {
        wrapped.respondHead( resource, response, request );
    }

	@Override
    public void respondExpectationFailed( Response response, Request request ) {
        wrapped.respondExpectationFailed( response, request );
    }

	@Override
    public void respondBadRequest( Resource resource, Response response, Request request ) {
        wrapped.respondBadRequest( resource, response, request );
    }

	@Override
    public void respondForbidden( Resource resource, Response response, Request request ) {
        wrapped.respondForbidden( resource, response, request );
    }

	@Override
    public void respondServerError( Request request, Response response, String reason ) {
        wrapped.respondServerError( request, response, reason );
    }

	@Override
    public void respondDeleteFailed( Request request, Response response, Resource resource, Status status ) {
        List<HrefStatus> statii = new ArrayList<HrefStatus>();
        statii.add( new HrefStatus( request.getAbsoluteUrl(), status ) );
        responseMultiStatus( resource, response, request, statii );

    }

	@Override
    public void respondPropFind( List<PropFindResponse> propFindResponses, Response response, Request request, Resource r ) {
        log.trace("respondPropFind");
        response.setStatus( Status.SC_MULTI_STATUS );
        response.setDateHeader( new Date() );
		response.setContentTypeHeader( "application/xml; charset=utf-8" );
        //response.setContentTypeHeader( Response.XML );
        List<String> supportedLevels = resourceTypeHelper.getSupportedLevels( r );
        String s = Utils.toCsv( supportedLevels );
        response.setDavHeader( s );
		
        String xml = propFindXmlGenerator.generate( propFindResponses );
        byte[] arr;
        try {
            arr = xml.getBytes( "UTF-8" );
        } catch( UnsupportedEncodingException ex ) {
            throw new RuntimeException( ex );
        }
        response.setContentLengthHeader( (long) arr.length );
        response.setEntity(new ByteArrayEntity(arr));
    }

	@Override
    public void respondInsufficientStorage( Request request, Response response, StorageErrorReason storageErrorReason ) {
        response.setStatus( Status.SC_INSUFFICIENT_STORAGE );
    }

	@Override
    public void respondLocked( Request request, Response response, Resource existingResource ) {
        response.setStatus( Status.SC_LOCKED );
    }

	@Override
    public void respondPreconditionFailed( Request request, Response response, Resource resource ) {
        response.setStatus( Status.SC_PRECONDITION_FAILED );
    }

	@Override
    public BUFFERING getBuffering() {
        if( wrapped instanceof Bufferable) {
            return ((Bufferable)wrapped).getBuffering();
        } else {
            throw new RuntimeException( "Wrapped class is not a known type");
        }
    }

	@Override
    public void setBuffering( BUFFERING buffering ) {
        if( wrapped instanceof Bufferable) {
            ((Bufferable)wrapped).setBuffering( buffering );
        } else {
            throw new RuntimeException( "Wrapped class is not a known type");
        }
    }
}
