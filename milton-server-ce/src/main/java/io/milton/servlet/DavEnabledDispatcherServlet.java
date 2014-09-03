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

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;

/**
 *  Subclasses DispatcherServlet to override logic which filters out requests
 * for webdav methods such as PROPFIND
 *
 * I don't know what the spring guys were thinking when they decided to do that,
 * but at least they made it easy to override.
 *
 * Hope they don't change it in a later release, could easily break this class
 *
 * Note that this class doesnt change the behaviour of the DispatcherServlet in
 * any other way so can be used as a drop in replacement
 */
public class DavEnabledDispatcherServlet extends DispatcherServlet{

    /**
     * Override of the default implementation to enable webdav methods
     *
     * @param req
     * @param resp
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doService(req, resp);
        } catch(ServletException e) {
            throw e;
        } catch(IOException e) {
            throw e;
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
    

}
