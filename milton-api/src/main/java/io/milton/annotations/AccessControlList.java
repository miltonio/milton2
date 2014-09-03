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
