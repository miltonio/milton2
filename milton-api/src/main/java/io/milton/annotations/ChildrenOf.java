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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Marks a method as one which locates children for the given parent. The parent
 * will always be the first method argument
 * 
 * There may be multiple matching childrenOf methods for a given parent object,
 * in which case all of the results are merged into a single set
 * 
 * The method must
 *  - return a collection, or array, or a single pojo object which has appropriate controllers
 *  - the first argument must be the hierachial parent of these objects
 * 
 * Example:
 *  @ChildrenOf
    public List<Band> getBands(BandsController root) {
        return Band.findAll(SessionManager.session());
    }
 *
 * Performance Tip
 * Note that milton will scan children collections to locate single objects
 * as part of resource location if no @ChildOf method is present. It will
 * also scan by default if any @ChildOf methods have returned null. This
 * can be a performance problem in many cases. To prevent @ChildrenOf methods
 * being used to locate single items set the allowChildLookups property to false
 * 
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChildrenOf {
    /**
     * If true (default) then the method this annotates can be used to locate single
     * items. Otherwise this method will be ignored for single child lookups
     * 
     * @return 
     */
    boolean allowChildLookups() default true;
    
    /**
     * If true this method will replace (ie override) calls to any other methods
     * with a target base class
     * 
     * For example if you have @ChildrenOf method targeting Animal, and another
     * method targeting Cat, then by default both sets of resources will be combined
     * to produce children for a source object of Cat. But by setting override to 
     * true only the Cat method will be used
     * 
     * @return 
     */
    boolean override() default false;
}
