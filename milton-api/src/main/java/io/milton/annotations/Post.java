/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * Typically used with ajax requests
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
     */
    boolean bindData() default false;
}
