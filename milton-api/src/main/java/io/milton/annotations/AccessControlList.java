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
 * Marks a method as one which returns the Access Control List for a user
 * on a given resource
 * 
 * The method must have at least 2 parameters
 *  <ul>
 *  <li>The object to return an access control list for. For example, if you want to return permissions for a Calendar, then this should be a Calendar</li>
 *  <li>The current user. This is the object returned by the @Authenticate method</li>
 * </ul>
 * 
 * Milton will search up the object hierarchy to find a @AccessControlList method. The
 * first one found will be used
 * 
 * So if a request is made to a URL like this: /users/brad/calenars/cal1
 * 
 * Then if cal1 represents an instance of a MyCalendar, and @Authenticate returned
 * an instance of MyUser, then you should have a method like this:
 * 
 * @AccessControlList public List<AccessControlledResource.Priviledge> getUserPrivs(MyCalendar target, MyUser currentUser)
 * 
 * But if you want to apply permissions at a higher level, say at the level of the user
 * object, and lets say that "brad" is an instance of a MyUser object then you would have this:
 * 
 * @AccessControlList public List<AccessControlledResource.Priviledge> getUserPrivs(MyUser target, MyUser currentUser)
 * 
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessControlList {

}
