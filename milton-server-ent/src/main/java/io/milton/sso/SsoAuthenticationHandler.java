/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * 
 */
package io.milton.sso;

import io.milton.http.AuthenticationHandler;
import io.milton.http.Request;
import io.milton.resource.Resource;
import java.util.List;

/**
 * This is a post resource-resolution authentication handler.
 *
 * It assumes that the SsoResourceFactory has populated the _sso_user request
 * attribute if appropriate
 *
 * @author brad
 */
public class SsoAuthenticationHandler implements AuthenticationHandler {

    @Override
    public boolean supports(Resource r, Request request) {
        boolean b = request.getAttributes().get("_sso_user") != null;
        return b;
    }

    @Override
    public Object authenticate(Resource resource, Request request) {
        return request.getAttributes().get("_sso_user");
    }

    @Override
    public void appendChallenges(Resource resource, Request request, List<String> challenges) {
    }

    @Override
    public boolean isCompatible(Resource resource, Request request) {
        return true;
    }

    @Override
    public boolean credentialsPresent(Request request) {
        return request.getParams() != null && request.getAttributes().containsKey("_sso_user");
    }
}
