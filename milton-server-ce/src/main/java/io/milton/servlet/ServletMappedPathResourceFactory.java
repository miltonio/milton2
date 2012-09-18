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

import io.milton.common.LogUtils;
import io.milton.http.ResourceFactory;
import io.milton.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For any requests which match the mapping path, the request is handled by
 * simply including a servlet resource at that path
 *
 * @author brad
 */
public class ServletMappedPathResourceFactory implements ResourceFactory {

    private static final Logger log = LoggerFactory.getLogger(ServletMappedPathResourceFactory.class);
    
    private String basePath;

    @Override
    public Resource getResource(String host, String path) {
        String contextPath = MiltonServlet.request().getContextPath();
        String localPath = path.substring(contextPath.length());
        if( localPath.startsWith(basePath)) {
            LogUtils.trace(log, "getResource: matched path: ", localPath);
            return new ServletResource(localPath, MiltonServlet.request(), MiltonServlet.response());
        } else {
            LogUtils.trace(log, "getResource: did not match path: requested:", localPath, "base:", basePath);
            return null;
        }
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
    
    

}