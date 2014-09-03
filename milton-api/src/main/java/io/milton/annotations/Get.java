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
package io.milton.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as one which generates content for the given resource
 * 
 * <p>The method return type can be
 * <ul>
 *  <li>void, in which case you must write content to the outputStream in the method call
 *  <li>a byte[] which is written to the output
 *  <li>an InputStream, which will be written to output, then closed (in a try/finally block)
 *  <li>a String, which is interpreted as a template name. The ViewResolver is used to locate a template with the
 * given name, and it is invoked with a model object with name "resource"
 *  <li>a {@link io.milton.common.ModelAndView} which is passed to the ViewResolver to execute the template
 * </ul>
 * Any other return type causes an exception
 * 
 * <p>The input parameters must be:
 * <ul>
 *  <li>first must be the source object
 *  <li>then any of Request, Response, Range, contentType (String), parameters (Map), OutputStream
 * </ul>
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Get {
    
    
    /**
     * If specified, will only match on requests with the given content type
     * 
     * @return 
     */
    String contentType() default "";
    
    /**
     * If present, only requests which contain all of the given parameters will
     * be matched
     * 
     * @return 
     */
    String[] params() default {};
    
    /**
     * Default is -1 which means use the system default. Returning 0 will 
     * explicitly disabled caching. Any other value is the number of seconds
     * that the content may be cached by clients
     * 
     * @return 
     */
    long maxAgeSecs() default -1;
}
