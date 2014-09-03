/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * 
 */
package io.milton.sso;

import io.milton.common.Path;
import io.milton.http.HttpManager;
import io.milton.resource.Resource;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is intended to be used with the SsoAuthenticationHandler to
 * provide path based authentication.
 *
 * For example, if a user logs in via a web page and there session id is ABC123,
 * they might be able to edit documents in MS Word without being challenged for
 * a login on a path like /ABC123/mydoc.doc
 *
 * To use this class, you should use it to wrap your existing resource factory.
 * Eg
 *
 * MyResourceFactory rf = new MyResourceFactory(); SsoResourceFactory ssoRf =
 * new SsoResourceFactory(rf);
 *
 * And then connect ssoRf to the HttpManager like this:
 *
 * HttpManager hm = new HttpManager(ssoRf);
 *
 * URL's which do not relate to SSO will be delegated to the wrapped resource
 * factory.
 *
 * When a SSO request is received, this resource factory will use the adapted
 * path to locate a resource with the wrapped resource factory.
 *
 * @author brad
 */
public class SsoResourceFactory implements ResourceFactory {

    private static final Logger log = LoggerFactory.getLogger(SsoResourceFactory.class);
    private final ResourceFactory resourceFactory;
    private final SsoSessionProvider ssoSessionProvider;

    public SsoResourceFactory(ResourceFactory resourceFactory, SsoSessionProvider ssoSessionProvider) {
        this.resourceFactory = resourceFactory;
        this.ssoSessionProvider = ssoSessionProvider;
    }

    @Override
    public Resource getResource(String host, String url) throws NotAuthorizedException, BadRequestException {
        Path p = Path.path(url);
        String firstComp = p.getFirst();
        Object oUserTag = null;
        if (firstComp != null) {
            oUserTag = ssoSessionProvider.getUserTag(firstComp);
        }

        if (oUserTag == null) {
            log.trace("not a SSO path");
            return resourceFactory.getResource(host, url);
        } else {
            log.trace("is an SSO path");
            Path strippedPath = p.getStripFirst();
            HttpManager.request().getAttributes().put("_sso_user", oUserTag);
            return resourceFactory.getResource(host, strippedPath.toString());
        }
    }
}
