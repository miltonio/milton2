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
 * Marks a method as one which can be used to authenticate a user. This is done
 * in 3 possible ways:
 * <ul>
 *  <li>return a password. This allows Milton to authenticate the user given a variety on inputs</li>
 *  <li>method which takes a requested password and verifies it. This is useful for Basic authentication if you store hashed passwords</li>
 *  <li>method which takes a Digest request and verifies it. This is useful for supporting Digest authentication if you store hashed passwords</li>
 * </ul>
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Authenticate {

}
