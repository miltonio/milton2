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
