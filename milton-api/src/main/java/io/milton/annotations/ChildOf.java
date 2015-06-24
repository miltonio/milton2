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
 * Marks a method as one which locates a child of a parent with a given name. This
 * will be used in preference to ChildrenOf for locating single items. If not
 * present Milton will iterate over the list of children.
 * 
 * <p>Optionally, ChildOf methods may be marked as user locator methods by setting 
 * the isUser property of the annotation to true. If this is done then the method
 * will be used for authentication, and the returned object will be used to derive
 * an Access Control List which will be used for authorization.
 * 
 * <p>Note that to perform authentication you MUST use a ChildOf method, you cannot
 * use a ChildrenOf method
 * 
 * <p>The method must:
 * <ul>
 *  <li>return a single object. This must be the same object as would be returned in a corresponding ChildrenOf object (ie both may be called in a single request)</li>
 *  <li>the first argument must be the hierarchical parent of these objects</li>
 *  <li>the second argument must be the name of the resource</li>
 * </ul>
 * 
 * Eg
 * <pre>
 * {@literal @}ChildOf
 * public User findUser(UsersHome homeFolder, String userId) {
 *    return UserDao.findUser(userId);
 * }
 * </pre>
 * 
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChildOf {
    
    /**
     * Will only match on paths which end with the given suffix. Default is empty
     * string so will always match
     * 
     * @return 
     */
    String pathSuffix() default ""; 
}
