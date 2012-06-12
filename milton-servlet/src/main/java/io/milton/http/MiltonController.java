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

package io.milton.http;

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
