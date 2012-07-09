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

package io.milton.resource;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import java.util.Date;


/**
 * 
 * Implementations should implement compareTo as an alphabetic comparison 
 *  on the name property
 * 
 * @author Alienware1
 */
public interface Resource {

    /**
     * Returning a null value is allowed, and disables the ETag field.
     * <P/>
     * If a unique id is returned it will be combined with the modified date (if available)
     * to produce an ETag which identifies this version of this resource. Note that this
     * behaviour can be changed by injecting an alternative EtagGenerator instance into
     * the HttpManagerBuilder
     * 
     * @return - a string which uniquely identifies this resource. This will be
     * used in the ETag header field, and affects caching of resources. 
     * 
     */
    String getUniqueId();
    
    /**
     * Note that this name MUST be consistent with URL resolution in your ResourceFactory
     * <P/>
     * If they aren't consistent Milton will generate a different href in PropFind
     * responses then what clients have request and this will cause either an
     * error or no resources to be displayed
     * 
     * @return - the name of this resource. Ie just the local name, within its folder
     */
    String getName();    
    
    
    /**
     * Check the given credentials, and return a relevant object if accepted.
     * <P/>
     * Returning null indicates credentials were not accepted
     * 
     * @param user - the user name provided by the user's agent
     * @param password - the password provided by the user's agent
     * @return - if credentials are accepted, some object to attach to the Auth object.
     * otherwise null
     */
    Object authenticate(String user, String password);

    /** Return true if the current user is permitted to access this resource using
     *  the specified method.
     *  <P/>
     *  Note that the current user may be determined by the Auth associated with
     *  the request, or by a separate, application specific, login mechanism such
     *  as a session variable or cookie based system. This method should correctly
     *  interpret all such mechanisms
     *  <P/>
     *  The auth given as a parameter will be null if authentication failed. The
     *  auth associated with the request will still exist
     */
    boolean authorise(Request request, Request.Method method, Auth auth);

    /** Return the security realm for this resource. Just any string identifier.
     * <P/>
     * This will be used to construct authorization challenges and will be used
     * on Digest authentication to construct the expected response.
     */
    String getRealm();

    /** The date and time that this resource, or any part of this resource, was last
     *  modified. For dynamic rendered resources this should consider everything
     *  which will influence its output.
     *<P/>
     *  Resources for which no such date can be calculated should return null.
     *<P/>
     *  This field, if not null, is used to reply to conditional GETs (ie GET with
     * if-modified-since). If the modified-since argument is later then the modified
     * date then we return a 304 - Not Modified.
     *<P/>
     * Although nulls are explicitly allowed by milton, certain client applications
     * might require modified dates for file browsing. For example, the command line
     * client on Vista doesn't work properly if this is null.
     *
     */
    Date getModifiedDate();



    
    /** Determine if a redirect is required for this request, and if so return
     *  the URL to redirect to. May be absolute or relative.
     *<P/>
     *  Called after authorization check but before any method specific processing
     *<P/>
     *  Return null for no redirect
     */
    String checkRedirect(Request request) throws NotAuthorizedException, BadRequestException;


}
