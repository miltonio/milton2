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
 * Marks a method as one which creates a collection within the resource given
 * 
 * <p>The method must
 * <ul> 
 *  <li>return the created object.
 *  <li>first param is the hierarchy parent objet (ie object which represents the parent folder)
 *  <li>the second param must be the name for the new object
 *  <li>other params are standard options such as request and response
 * </ul>
 * 
 * <p>Eg
 * <pre>
 *  {@literal @}MakeCollection
    public Band createBand(BandsController root, String newName) {
		..
	}
	</pre>
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MakeCollection {
    
}
