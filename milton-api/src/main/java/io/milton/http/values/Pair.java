/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.milton.http.values;

/**
 * Represents an object that constitutes with two related objects. 
 * 
 * Example: 
 * Xml Attribtes -> name,value
 * 
 * @author nabil.shams
 */
public class Pair<T,U> {
	private T object1;
	private U object2;
	public Pair(T t, U u){
		object1 = t;
		object2 = u;
	}

	/**
	 * @return the object1
	 */
	public T getObject1() {
		return object1;
	}

	/**
	 * @return the object2
	 */
	public U getObject2() {
		return object2;
	}
}
