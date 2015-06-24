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
 * Marks a method as one which returns user objects. A "user" object is any
 * pojo which can be considered a user. To be usable as a user it must at least be
 * able to be authenticated by having a password field, or explicit methods for
 * validating a password or Digest hash
 * 
 * <p>@Users methods MUST take a root folder collection object as their parent. For example /users/brad
 * 
 * <p>Optionally, user objects may be supported by methods to return an access control
 * list which can be used for authorization. Otherwise a default ACL scheme is used
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Users {

}
