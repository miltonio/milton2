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

import io.milton.http.AuthenticationHandler;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.resource.Resource;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Supports authentication from form parameters.
 *
 * Note that this will not by itself result in a persistent login. It should
 * be used with cookie authentication handler, or some equivalent session
 * based handler
 *
 * @author brad
 */
public class FormAuthenticationHandler implements AuthenticationHandler {

    private static final Logger log = LoggerFactory.getLogger( FormAuthenticationHandler.class );
    private String userNameParam = "_loginUserName";
    private String passwordParam = "_loginPassword";
	private String loginResultAttName = "loginResult";

    @Override
    public boolean supports( Resource r, Request request ) {
        // We will support it if its a form POST and a username param is present
        boolean b = isLogin( request );
        if( log.isTraceEnabled() ) {
            log.trace( "supports: " + b );
        }
        return b;
    }

    /**
     * The authentication result is written to a request attribute called "loginResult".
     *
     * Its value is "true" if login succeeded and "false" if not. Note that a
     * successful login does not ensure that that authorisation will succeed.
     *
     * If rendering a login page based on authentication and authorisation you should also look at the
     * "authReason" attribute set by the LoginResponseHandler which gives the
     * reason for an authorisation failure
     *
     * @param resource
     * @param request
     * @return
     */
    @Override
    public Object authenticate( Resource resource, Request request ) {
        String userName = request.getParams().get( userNameParam );
        String pwd = request.getParams().get( passwordParam );
        Object o = resource.authenticate( userName, pwd );
        // set a request attribute that can be used when rendering
        if( o == null ) {
			log.trace("Form authentication failed");
            request.getAttributes().put( "loginResult", Boolean.FALSE);
        } else {
			log.trace("Form authentication succeeded");
            request.getAttributes().put( "loginResult", Boolean.TRUE);
        }
        return o;
    }

	@Override
	public boolean credentialsPresent(Request request) {
		return request.getParams() != null && request.getParams().containsKey(userNameParam);
	}	
	
    @Override
    public void appendChallenges( Resource resource, Request request, List<String> challenges ) {
        // doesnt do http challenge
    }

    @Override
    public boolean isCompatible( Resource resource, Request request ) {
        // never issue challenge
        return false;
    }

    private boolean isLogin( Request request ) {
        return ( request.getMethod() == Method.POST && request.getParams().get( userNameParam ) != null );
    }

    public String getUserNameParam() {
        return userNameParam;
    }

    public void setUserNameParam( String userNameParam ) {
        this.userNameParam = userNameParam;
    }

    public String getPasswordParam() {
        return passwordParam;
    }

    public void setPasswordParam( String passwordParam ) {
        this.passwordParam = passwordParam;
    }

	/**
	 * Get the attribute name used to store the login result. The login result
	 * is a Boolean which indicates if authentication was successful or not. A
	 * null value indicates that authentication was not attempted
	 * 
	 * @return 
	 */
	public String getLoginResultAttName() {
		return loginResultAttName;
	}

	public void setLoginResultAttName(String loginResultAttName) {
		this.loginResultAttName = loginResultAttName;
	}
	
	
}
