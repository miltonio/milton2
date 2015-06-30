/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.annotations;

import io.milton.resource.AccessControlledResource;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as one which handles POST requests for a given resource
 * 
 * <p>Typically used with AJAX requests.
 * 
 * <p>The return value can either be a String, which is interpreted as a redirect URL;
 * or it can be a JsonResult, which will be serialized back to the client
 * 
 * <p>If {@code null} is returned then a response will be generated as if the request was
 * a GET
 *  
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Post {
    

    /**
     * The priviledge required to execute this POST request. Note that the semantics
     * of POST can vary widely, so can't easily be tied to a single priviledge. For
     * example, commenting on an article is often done with a POST and it might be appropriate
     * to permit comments from anyone with READ_CONTENT.
     * 
     * On the other hand, POST might also be used for deleting comments which is usually
     * an administrator action so might require WRITE_CONTENT
     * 
     * @return 
     */
    AccessControlledResource.Priviledge requiredPriviledge() default AccessControlledResource.Priviledge.READ_CONTENT;

    /**
     * If present, only requests which contain all of the given parameters will
     * be matched
     * 
     * @return 
     */
    String[] params() default {};
    
    /**
     *  If true, milton will attempt to bind request parameters to the source object
     * 
     *  If values are present and cannot be bound, will return a JsonResult containing
     *  field validation failures
     * 
     *  For this reason you should ONLY use data binding with AJAX posts
     * @return 
     */
    boolean bindData() default false;
    
    /**
     * If present, and not empty, identifies a request parameter which 
     * contains the timezone ID in the form post to use for parsing
     * date/time values
     * 
     * If empty the platform default will be used
     * 
     * @return 
     */
    String timeZoneParam() default "";
}
