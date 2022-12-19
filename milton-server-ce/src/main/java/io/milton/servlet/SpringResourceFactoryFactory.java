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
import io.milton.http.ResourceFactory;
import io.milton.http.webdav.WebDavResponseHandler;
import jakarta.servlet.ServletException;
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
public class SpringResourceFactoryFactory implements MiltonConfigurator{

    ApplicationContext context;
    
    private final HttpManagerBuilder configurer = new HttpManagerBuilder();

    private HttpManager httpManager;
    
    @Override
    public HttpManager configure(Config config) throws ServletException {
        context = new ClassPathXmlApplicationContext(new String[] {"applicationContext.xml"});
        ResourceFactory rf = (ResourceFactory) context.getBean("milton.resource.factory");
        WebDavResponseHandler davResponseHandler = (WebDavResponseHandler) context.getBean("milton.response.handler");
        configurer.setMainResourceFactory(rf);
        configurer.setWebdavResponseHandler(davResponseHandler);
        httpManager = configurer.buildHttpManager();
        return httpManager;
    }

    @Override
    public void shutdown() {
        if( httpManager != null ) {
            httpManager.shutdown();
        }
    
    }    

}
