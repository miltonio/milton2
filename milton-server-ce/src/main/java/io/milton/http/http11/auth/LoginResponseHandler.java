/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.milton.http.http11.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.milton.http.*;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.http.json.ObjectMapperFactory;
import io.milton.http.webdav.WebDavResponseHandler;
import io.milton.resource.GetableResource;
import io.milton.resource.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This decorates a wrapped response handler, and gives it the ability to
 * generate login pages. When activated, this will suppress the http
 * authorisation status code and instead render a login page.
 * <p>
 * Note that the conditions under which a login page is produced in place of a
 * http challenge are quite specific and should not interfere with non web
 * browser user agents.
 * <p>
 * This will usually be used together with FormAuthenticationHandler and
 * CookieAuthenticationHandler to provide a complete authentication mechanism
 * integrated into the normal milton life cycle
 *
 * @author brad
 */
public class LoginResponseHandler extends AbstractWrappingResponseHandler {

    private static final Logger log = LoggerFactory.getLogger(LoginResponseHandler.class);
    public static final String ATT_DISABLE = "auth.disable.html";

    /**
     * Called when authentication has detected a user type which is not
     * compatible with form authentication. This will prevent a html form being
     * presented, so the user will be forced to login via Basic or Diget
     *
     * @param r
     */
    public static void setDisableHtmlResponse(Request r) {
        log.trace("html login response disabled for this request");
        r.getAttributes().put(ATT_DISABLE, Boolean.TRUE);
    }

    private String loginPage = "/login.html";
    private final ResourceFactory resourceFactory;
    private final LoginPageTypeHandler loginPageTypeHandler;
    private List<String> excludePaths;
    private boolean enabled = true;

    public LoginResponseHandler(WebDavResponseHandler wrapped, ResourceFactory resourceFactory, LoginPageTypeHandler loginPageTypeHandler) {
        super(wrapped);
        this.resourceFactory = resourceFactory;
        this.loginPageTypeHandler = loginPageTypeHandler;
    }

    /**
     * If responding with a login page, the request attribute "authReason" is
     * set to either "required", indicating that the user must login; or
     * "notPermitted" indicating that the user is currently logged in but does
     * not have permission
     *
     * @param resource
     * @param response
     * @param request
     */
    @Override
    public void respondUnauthorised(Resource resource, Response response, Request request) {
        log.info("respondUnauthorised");
        //String acceptHeader = request.getAcceptHeader();
        Boolean disabled = (Boolean) request.getAttributes().get(ATT_DISABLE);
        if (disabled == null || !disabled) {
            if (isEnabled() && !excluded(request) && isGetOrPost(request)) {
                if (loginPageTypeHandler.canLogin(resource, request)) {
                    attemptRespondLoginPage(request, resource, response);
                    return;
                } else if (loginPageTypeHandler.isAjax(resource, request)) {
                    respondJson(request, response, resource);
                    return;
                }
            }
        } else {
            log.trace("html login form has been disabled for this request");
        }
        log.trace("respond with normal 401");
        wrapped.respondUnauthorised(resource, response, request);
    }

    private void attemptRespondLoginPage(Request request, Resource resource, Response response) throws RuntimeException {
        log.trace("attemptRespondLoginPage");
        Resource rLogin;
        try {
            rLogin = resourceFactory.getResource(request.getHostHeader(), loginPage);
        } catch (NotAuthorizedException | BadRequestException e) {
            throw new RuntimeException(e);
        }
        if (!(rLogin instanceof GetableResource gr)) {
            log.info("Couldnt find login resource: {}{} with resource factory: {}", request.getHostHeader(), loginPage, resourceFactory.getClass());
            wrapped.respondUnauthorised(resource, response, request);
        } else {
            log.trace("respond with 200 to suppress login prompt, using resource: {} - {}", rLogin.getName(), rLogin.getClass());
            try {
                // set request attribute so rendering knows it authorisation failed, or authentication is required
                Auth auth = request.getAuthorization();
                if (auth != null && auth.getTag() != null) {
                    // no authentication was attempted,
                    request.getAttributes().put("authReason", "notPermitted");
                } else {
                    request.getAttributes().put("authReason", "required");
                }

                response.setStatus(Response.Status.SC_BAD_REQUEST); // error code to avoid caching
                gr.sendContent(response.getOutputStream(), null, null, gr.getContentType(null));
                response.getOutputStream().flush();
                //wrapped.respondContent(gr, response, request, null);

            } catch (NotAuthorizedException | IOException | NotFoundException | BadRequestException ex) {
                response.setStatus(Response.Status.SC_INTERNAL_SERVER_ERROR);
                response.close();
                log.error("Exception generating login page", ex);
            }
        }
    }

    public String getLoginPage() {
        return loginPage;
    }

    public void setLoginPage(String loginPage) {
        this.loginPage = loginPage;
    }

    public ResourceFactory getResourceFactory() {
        return resourceFactory;
    }

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
    }

    private boolean excluded(Request request) {
        if (CollectionUtils.isEmpty(excludePaths)) {
            return false;
        }
        for (String s : excludePaths) {
            if (request.getAbsolutePath().startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private boolean isGetOrPost(Request request) {
        return request.getMethod().equals(Method.GET) || request.getMethod().equals(Method.POST);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private void respondJson(Request request, Response response, Resource resource) {
        Map<String, Object> map = new HashMap<>();
        Boolean loginResult = (Boolean) request.getAttributes().get("loginResult");
        map.put("loginResult", loginResult);
        Auth auth = request.getAuthorization();
        if (auth != null && auth.getTag() != null) {
            map.put("authReason", "notPermitted");
        } else {
            map.put("authReason", "required");
        }
        String userUrl = (String) request.getAttributes().get("userUrl");
        if (userUrl != null) {
            map.put("userUrl", userUrl);
        }
        response.setStatus(Response.Status.SC_BAD_REQUEST);
        response.setCacheControlNoCacheHeader();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            ObjectMapper mapper = ObjectMapperFactory.mapper();
            mapper.writeValue(bout, map);
            byte[] arr = bout.toByteArray();
            response.setContentLengthHeader((long) arr.length);

            response.getOutputStream().write(arr);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public interface LoginPageTypeHandler {

        /**
         * Return true if the given resource and request is suitable for
         * presenting a web browser login page
         *
         * @param r
         * @param request
         * @return
         */
        boolean canLogin(Resource r, Request request);

        /**
         * Return true if the request indicates that the login response should
         * be given as json data (ie response to an ajax login)
         *
         * @param r
         * @param request
         * @return
         */
        boolean isAjax(Resource r, Request request);
    }

    /**
     * Default implementation which uses some sensible rules about content types
     * etc
     */
    public static class ContentTypeLoginPageTypeHandler implements LoginPageTypeHandler {

        @Override
        public boolean canLogin(Resource resource, Request request) {
            if (resource instanceof GetableResource) {
                String ctHeader = request.getAcceptHeader();
                GetableResource gr = (GetableResource) resource;
                String ctResource = gr.getContentType("text/html");
                if (ctResource == null) {
                    if (ctHeader != null) {
                        boolean b = ctHeader.contains("html");
                        log.trace("isPage: resource has no content type, depends on requested content type: {}", b);
                        return b;
                    } else {
                        log.trace("isPage: resource has no content type, and no requeted content type, so assume false");
                        return false;
                    }
                } else {
                    boolean b = ctResource.contains("html");
                    log.trace("isPage: resource has content type. is html? {}", b);
                    return b;
                }
            } else {
                log.trace("isPage: resource is not getable");
                return false;
            }
        }

        @Override
        public boolean isAjax(Resource r, Request request) {
            String acceptHeader = request.getAcceptHeader();
            return acceptHeader != null && (acceptHeader.contains("application/json") || acceptHeader.contains("text/javascript"));
        }
    }
}
