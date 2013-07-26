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
 * Annotation type to identify classes to be accessible by
 * BeanPropertySource
 *
 * This allows them to have their properties read from and written to
 * by PROPFIND and PROPPATCH.
 *
 * @author brad
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BeanPropertyResource {

    /**
     * Default property which is the namespace uri for the properties
     * on this resource
     *
     * E.g. http://mycompany.com/ns/example
     * 
     * @return - the namespace uri
     */
    String value();
    /**
     *
     * @return - true allows the resource to be updatable
     */
    boolean writable() default true;

    /**
     * If true, indicates that properties on the resource should be accessible
     * unless otherwise specified
     * 
     * @return
     */
    boolean enableByDefault() default true;
}
