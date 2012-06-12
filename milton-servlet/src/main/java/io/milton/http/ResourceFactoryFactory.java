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

import io.milton.http.webdav.WebDavResponseHandler;

/**
 *  Implement this interface to configure the ResourceFactory instance
 * 
 * To use your implementation, specify its class name in: resource.factory.factory.class
 * as an init parameter on the servlet or filter in web.xml
 * 
 * Example:
 * <PRE>
 * {@code
 * <servlet>
 *   <servlet-name>milton</servlet-name>
 *   <servlet-class>io.milton.http.MiltonServlet</servlet-class>
 *     <init-param>
 *       <param-name>resource.factory.factory.class</param-name>
 *       <param-value>io.milton.http.SpringResourceFactoryFactory</param-value>
 *     </init-param>
 * </servlet>
 * }
 * </PRE>
 *
 * 
 */
public interface ResourceFactoryFactory {

    /**
     * Create and return a ResponseHandler. Normally this will be DefaultResponseHandler
     *
     * @return
     */
    public WebDavResponseHandler createResponseHandler();

    /**
     * Called immediately after construction
     */
    void init();
    
    /**
     * Create and return a ResourceFactory instance. This single instance
     * will usually be used for the lifetime of the servlet
     * 
     * @return 
     */
    ResourceFactory createResourceFactory();
}
