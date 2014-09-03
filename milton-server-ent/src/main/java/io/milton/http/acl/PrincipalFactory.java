/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */

package io.milton.http.acl;

import io.milton.principal.Principal;
import io.milton.http.Auth;

/**
 * Transforms various sources of user and group information into ACL compatible
 * Principal objects
 *
 * @author brad
 */
public interface PrincipalFactory {
	Principal fromAuth(Auth auth);
}
