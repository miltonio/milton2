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

package io.milton.http.http11;

import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;
import io.milton.http.HttpManager;
import io.milton.resource.GetableResource;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.common.ContentTypeUtils;
import io.milton.common.Path;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.common.LogUtils;
import java.io.IOException;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of utility methods for PutHandler
 *
 */
public class PutHelper {

    private static final Logger log = LoggerFactory.getLogger( PutHelper.class );

    /**
     * Largly copied from tomcat
     *
     * See the spec
     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
     *
     * @param r
     * @param request
     * @return
     * @throws IOException
     * @throws BadRequestException - if the range header is invalid
     */
    public Range parseContentRange(Resource r, Request request) throws IOException, BadRequestException {
        // Retrieving the content-range header (if any is specified
        String rangeHeader = request.getContentRangeHeader();
        if (rangeHeader == null) {
            return null;
        }

        // bytes is the only range unit supported
        if (!rangeHeader.startsWith("bytes")) {
            log.warn("Invalid range header, does not start with 'bytes': " + rangeHeader);
            throw new BadRequestException(r);
        }

        rangeHeader = rangeHeader.substring(6).trim();

        int dashPos = rangeHeader.indexOf('-');
        int slashPos = rangeHeader.indexOf('/');

        if (dashPos == -1) {
            log.warn("Invalid range header, dash not found: " + rangeHeader);
            throw new BadRequestException(r);
        }

        if (slashPos == -1) {
            log.warn("Invalid range header, slash not found: " + rangeHeader);
            throw new BadRequestException(r);
        }


        String s;

        long start;
        s = rangeHeader.substring(0, dashPos);
        try {
            start = Long.parseLong(s);
        } catch (NumberFormatException e) {
            log.warn("Invalid range header, start is not a valid number: " + s + " Raw header:" + rangeHeader);
            throw new BadRequestException(r);
        }

        long finish;
        s = rangeHeader.substring(dashPos + 1, slashPos);
        try {
            finish = Long.parseLong(s);
        } catch (NumberFormatException e) {
            log.warn("Invalid range header, finish is not a valid number: " + s + " Raw header:" + rangeHeader);
            throw new BadRequestException(r);
        }

        Range range = new Range(start, finish);


        if (!validate(range)) {
            throw new BadRequestException(r);
        }

        return range;
    }

    private boolean validate(Range r) {
        if( r.getStart() < 0 ) {
            log.warn("invalid range, start is negative");
            return false;
        } else if( r.getFinish() < 0 ) {
            log.warn("invalid range, finish is negative");
            return false;
        } else if( r.getStart() > r.getFinish()) {
            log.warn("invalid range, start is greater then finish");
            return false;
        } else {
            return true;
        }
    }


    public Long getContentLength( Request request ) throws BadRequestException {
        Long l = request.getContentLengthHeader();
        if( l == null ) {
            String s = request.getRequestHeader( Request.Header.X_EXPECTED_ENTITY_LENGTH );
            if( s != null && s.length() > 0 ) {
                log.debug( "no content-length given, but founhd non-standard length header: " + s );
                try {
                    l = Long.parseLong( s );
                } catch( NumberFormatException e ) {
                    throw new BadRequestException( null, "invalid length for header: " + Request.Header.X_EXPECTED_ENTITY_LENGTH.code + ". value is: " + s );
                }
            }
        }
        return l;
    }

    /**
     * returns a textual representation of the list of content types for the
     * new resource. This will be the content type header if there is one,
     * otherwise it will be determined by the file name
     *
     * @param request
     * @param newName
     * @return
     */
    public String findContentTypes( Request request, String newName ) {
//        String ct = request.getContentTypeHeader();
//        if( ct != null ) {
//			LogUtils.trace(log, "findContentTypes: got header: " + ct);
//			return ct;
//		}

        String s = ContentTypeUtils.findContentTypes( newName );
		LogUtils.trace(log, "findContentTypes: got type from name. Type: " + s);
		return s;
    }


    public CollectionResource findNearestParent( HttpManager manager, String host, Path path ) throws NotAuthorizedException, ConflictException, BadRequestException {
        if( path == null ) return null;

        Resource thisResource = manager.getResourceFactory().getResource( host, path.toString() );
        if( thisResource != null ) {
            if( thisResource instanceof CollectionResource ) {
                return (CollectionResource) thisResource;
            } else {
                log.warn( "parent is not a collection: " + path );
                return null;
            }
        }

        CollectionResource parent = findNearestParent( manager, host, path.getParent() );
        return parent;
    }

    /**
     * Copy the current content of the resource to the outputstream, except
     * writing the new partial update for the given range.
     *
     *
     * @param replacee - the resource to get the content for and to update
     * @param request
     * @param range
     * @param tempOut
     */
    public void applyPartialUpdate(GetableResource replacee, Request request, Range range, OutputStream tempOut) throws NotAuthorizedException, BadRequestException, NotFoundException {
        try {
            replacee.sendContent(tempOut, null, null, null);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
