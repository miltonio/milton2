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
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**  Specify this class name in your init parameter: resource.factory.factory.class
 * 
 * This will load applicationContext.xml from the classpath and use that to
 * load the ResourceFactory from bean id: milton.resource.factory
 * 
 * Example web.xml
 * <PRE>
 * {@code
 * <servlet>
 *   <servlet-name>milton</servlet-name>
 *   <servlet-class>io.milton.http.MiltonServlet</servlet-class>
 *   <init-param>
 *         <param-name>resource.factory.factory.class</param-name>
 *         <param-value>io.milton.http.SpringResourceFactoryFactory</param-value>
 *   </init-param>
 * </servlet>
 * }
 * </PRE>
 * 
 * Example applicationContext.xml
 * <PRE>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <beans xmlns="http://www.springframework.org/schema/beans"
 *        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *        xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">
 *
 *     <bean id="milton.resource.factory" class="com.ettrema.http.fs.FileSystemResourceFactory">
 *         <property name="securityManager" ref="milton.fs.security.manager" />
 *     </bean>
 *
 *     <bean id="milton.response.handler" class="com.ettrema.http.DefaultResponseHandler" />
 *
 *     <bean id="milton.fs.security.manager" class="com.ettrema.http.fs.NullSecurityManager" >
 *         <property name="realm" value="aRealm" />
 *     </bean>
 *
 * </beans>
  * }
 * </PRE>
 *
 */
public class SpringResourceFactoryFactory implements ResourceFactoryFactory{

    ApplicationContext context;

	@Override
    public void init() {
        context = new ClassPathXmlApplicationContext(new String[] {"applicationContext.xml"});
    }


    
	@Override
    public ResourceFactory createResourceFactory() {
        ResourceFactory rf = (ResourceFactory) context.getBean("milton.resource.factory");
        return rf;
    }

	@Override
    public WebDavResponseHandler createResponseHandler() {
        return  (WebDavResponseHandler) context.getBean("milton.response.handler");
    }



}
