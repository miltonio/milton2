/*
 * Copyright 2013 McEvoy Software Ltd.
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
 * 
 * Supports CardDAV Directory Gateway Extension
 * 
 * https://github.com/miltonio/milton2/issues/25
 * 
 * 
 * https://tools.ietf.org/html/draft-daboo-carddav-directory-gateway-02#page-4
 * 
 * Implement this for each address book class to return whether it is or is not
 * to be used as a directory gateway
 * 
 * MUST return Boolean !!
 * 
 * Default bean property is "directoryGateway"
 * 
 * Example annotated method:
 * 
 * @DirectoryGateway
 * public boolean isGateway(MyAddressBook ab) {
 *      return ab.isGateway(); // can be a constant or per addressbook
 * }
 * 
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DirectoryGateway {
    
}
