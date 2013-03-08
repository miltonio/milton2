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

package io.milton.servlet;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.HttpManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 * IMPORTANT !!!!!!!!!!! This controller will ONLY work if used in conjunction
 * with DavEnabledDispatcherServlet
 *
 * It WILL NOT work with the standard spring DispatcherServlet because it
 * explicitly forbids the use of webdav methods such as PROPFIND
 *
 * Please see the javadoc for DavEnabledDispatcherServlet for details
 *
 */
public class MiltonController implements Controller {

    private static final Logger log = LoggerFactory.getLogger(MiltonController.class);
    private HttpManager httpManager;

    public MiltonController() {
    }

    public MiltonController(HttpManagerBuilder config) {
        this.httpManager = config.buildHttpManager();
    }
    
    
    public MiltonController(HttpManager httpManager) {
        log.debug("created miltoncontroller");
        this.httpManager = httpManager;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("handleRequest: " + request.getRequestURI() + " method:" + request.getMethod());
        ServletRequest rq = new ServletRequest(request, null);
        ServletResponse rs = new ServletResponse(response);
        httpManager.process(rq, rs);
        return null;
    }

    public HttpManager getHttpManager() {
        return httpManager;
    }

    public void setHttpManager(HttpManager httpManager) {
        this.httpManager = httpManager;
    }
}
