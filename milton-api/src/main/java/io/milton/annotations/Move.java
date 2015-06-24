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
 * Marks a method as one which moves a resource to a new parent, or that renames it, or both
 * 
 * <p>Return type - void
 * 
 * <p>Input parameters:
 * <ul> 
 *  <li>source object
 *  <li>destination parent (as POJO) - might be same as current parent, indicating no change
 *  <li>newName - might be same as current name, indicating no change
 * </ul>
 * 
 * <p>Example:
 * <pre>
 *  {@literal @}Move
    public void move(MyDatabase.AbstractContentItem source, MyDatabase.FolderContentItem newParent, String newName) {
        source.moveTo(newParent);
        source.setName(newName);
    }
    </pre>
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Move {
    
}
