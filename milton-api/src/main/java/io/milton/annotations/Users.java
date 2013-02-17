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
 * Marks a method as one which returns user objects. A "user" object is any
 * pojo which can be considered a user. To be useable as a user it must at least be
 * able to be authenticated by having a password field, or explicit methods for
 * validating a password or Digest hash
 * 
 * @Users methods MUST take a root folder collection object as their parent. For example /users/brad
 * 
 * 
 * Optionally, user objects may be supported by methods to return an access control
 * list which can be used for authorisation. Otherwise a default ACL scheme is used
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Users {

}
