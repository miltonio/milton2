/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
