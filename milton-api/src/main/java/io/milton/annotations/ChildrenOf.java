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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as one which locates children for the given parent. The parent
 * will always be the first method argument
 * 
 * <p>There may be multiple matching childrenOf methods for a given parent object,
 * in which case all of the results are merged into a single set
 * 
 * <p>The method must:
 * <ul>
 *  <li>return a collection, or array, or a single POJO object which has appropriate controllers
 *  <li>the first argument must be the hierarchical parent of these objects
 * </ul>
 * 
 * <p>Example:
 * <pre>
 *  {@literal @}ChildrenOf
 *    public List{@literal <}Band{@literal >} getBands(BandsController root) {
 *        return Band.findAll(SessionManager.session());
 *    }
 * </pre>
 *
 * <strong>Performance Tip</strong>
 * Note that Milton will scan children collections to locate single objects
 * as part of resource location if no {@code @ChildOf} method is present. It will
 * also scan by default if any {@code @ChildOf} methods have returned null. This
 * can be a performance problem in many cases. To prevent {@code @ChildrenOf} methods
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
