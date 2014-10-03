/*
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

package io.milton.resource;

/**
 * Implement for resources that want to be able to update their display names
 * 
 * Note that most webdav clients do not use displayname, but it is used for
 * calendars and addressbooks
 *
 * @author brad
 */
public interface DisplayNameResource extends PropFindableResource {
    String getDisplayName();
    void setDisplayName(String s);
    
}
