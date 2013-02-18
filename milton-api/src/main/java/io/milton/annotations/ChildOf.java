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

import io.milton.common.Utils;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Marks a method as one which locates a child of a parent with a given name. This
 * will be used in preference to ChildrenOf for locating single items. If not
 * present milton will iterate over the list of children.
 * 
 * Optionally, ChildOf methods may be marked as user locator methods by setting 
 * the isUser property of the annotation to true. If this is done then the method
 * will be used for authentication, and the returned object will be used to derive
 * an Access Control List which will be used for authorisation.
 * 
 * Note that to perform authentication you MUST use a ChildOf method, you cannot
 * use a ChildrenOf method
 * 
 * The method must
 *  - return a single object. This must be the same object as would be returned in a corresponding ChildrenOf object (ie both may be called in a single request)
 *  - the first argument must be the hierachial parent of these objects
 *  - the second argument must be the name of the resource
 * 
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChildOf {
    
    /**
     * Will only match on paths which end with the given suffic. Default is empty
     * string so will always match
     * 
     * @return 
     */
    String pathSuffix() default ""; 
}
