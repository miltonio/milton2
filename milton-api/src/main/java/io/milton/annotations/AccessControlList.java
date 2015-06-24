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
 * Marks a method as one which returns the Access Control List for a user
 * on a given resource
 * 
 * <p>The method must have at least 2 parameters:
 *  <ul>
 *  <li>The object to return an access control list for. For example, if you want to return permissions for a Calendar, then this should be a Calendar</li>
 *  <li>The current user. This is the object returned by the {@code @Authenticate} method</li>
 * </ul>
 * 
 * <p>Milton will search up the object hierarchy to find a {@code @AccessControlList} method. The
 * first one found which returns a non-null value will be used. Returning null
 * indicates the method is not able to determine an ACL, so Milton will continue the search up the parents.
 *
 * <p>So if a request is made to a URL like this: {@code /users/brad/calenars/cal1}
 * 
 * <p>Then if cal1 represents an instance of a MyCalendar, and {@code @Authenticate} returned
 * an instance of MyUser, then you should have a method like this:
 * 
 * <pre>{@code @AccessControlList public List{@literal <}AccessControlledResource.Priviledge{@literal >} getUserPrivs(MyCalendar target, MyUser currentUser)}</pre>
 * 
 * <p>But if you want to apply permissions at a higher level, say at the level of the user
 * object, and lets say that "brad" is an instance of a MyUser object then you would have this:
 * 
 * <pre>{@code {@literal @}AccessControlList public List{@literal <}AccessControlledResource.Priviledge{@literal >} getUserPrivs(MyUser target, MyUser currentUser)}</pre>
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessControlList {

}
