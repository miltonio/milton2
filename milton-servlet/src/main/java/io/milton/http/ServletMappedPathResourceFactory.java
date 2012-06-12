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

import io.milton.common.LogUtils;
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