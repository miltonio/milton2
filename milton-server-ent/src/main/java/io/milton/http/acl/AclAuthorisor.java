/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */

package io.milton.http.acl;

import io.milton.common.LogUtils;
import io.milton.resource.AccessControlledResource;
import io.milton.resource.AccessControlledResource.Priviledge;
import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.principal.Principal;
import io.milton.property.PropertyAuthoriser;
import io.milton.resource.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class which performs authorisation of requests based on information
 * exposed through the AccessControlledResource interface
 * 
 * To use this class you must connect it to a io.milton.http.SecurityManager
 * or in the implementation of Resource.authorise
 *
 * @author brad
 */
public class AclAuthorisor implements PropertyAuthoriser {
	
	private static final Logger log = LoggerFactory.getLogger( AclAuthorisor.class );
	
	private final PrincipalFactory principalFactory;

	public AclAuthorisor(PrincipalFactory principalFactory) {
		this.principalFactory = principalFactory;
	}
		
	
	/**
	 * Attempt to determine if the request should be allowed. Note that some
	 * priviledges may apply at a field level, and this method does NOT check
	 * field level priviledges. That must be done seperately as part of the PROPFIND
	 * or PROPPATCH processing
	 * 
	 * @param request - the current request
	 * @param method - the HTTP method being invoked
	 * @param auth - the authentication object for the current request
	 * @param resource - the resource being acted on
	 * @return - true indicates that the request should be allowed, false that it 
	 * should not and null indicates that this class has no opinion
	 */
	public Boolean authorise( Request request, Method method, Auth auth, Resource resource ) {
		LogUtils.trace(log, "authorise", request.getAbsoluteUrl(), method.code, auth.getUser(), resource.getName());
		Principal currentPrincipal;
		List<Priviledge> list;
		if( resource instanceof AccessControlledResource) {
			AccessControlledResource acr = (AccessControlledResource) resource;
			Map<Principal, List<Priviledge>> privs = acr.getAccessControlList();
			if( privs == null ) {
				return null;
			} else {
				currentPrincipal = principalFactory.fromAuth(auth);
				list = privs.get(currentPrincipal);
				for( Priviledge p : list ) {
					if( method.isWrite ) {
						if( p.equals(Priviledge.WRITE)) {
							log.trace("found write permission");
							return true;
						}
					} else {
						if( p.equals(Priviledge.READ)) {
							log.trace("found read permission");
							return true;
						}
					}
				}
				log.trace("did not find applicable permission");
				return false;
			}
		} else {
			return null;
		}
	}

	/**
	 * Implements authorisation checks for specific ACL properties
	 * 
	 * @param request
	 * @param method
	 * @param perm
	 * @param fields
	 * @param resource
	 * @return 
	 */
	@Override
	public Set<CheckResult> checkPermissions(Request request, Method method, PropertyPermission perm, Set<QName> fields, Resource resource) {
		return null; // TODO!!!
	}

	
}
