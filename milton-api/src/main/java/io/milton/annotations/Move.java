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
 * Marks a method as one which moves a resource to a new parent, or that renames it, or both
 * 
 * Return type - void
 * 
 * Input params: 
 *  - source object
 *  - destination parent (as pojo) - might be same as current parent, indicating no change
 *  - newName - might be same as current name, indicating no change
 * 
 * Example:
 *     @Move
    public void move(MyDatabase.AbstractContentItem source, MyDatabase.FolderContentItem newParent, String newName) {
        source.moveTo(newParent);
        source.setName(newName);
    }
    
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Move {
    
}
