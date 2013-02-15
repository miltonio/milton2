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
import java.util.List;

/**
 * Marks a method as one which locates children for the given parent. The parent
 * will always be the first method argument
 * 
 * There may be multiple matching childrenOf methods for a given parent object,
 * in which case all of the results are merged into a single set
 * 
 * The method must
 *  - return a collection, or array, or a single pojo object which has appropriate controllers
 *  - the first argument must be the hierachial parent of these objects
 * 
 * Example:
 *  @ChildrenOf
    public List<Band> getBands(BandsController root) {
        return Band.findAll(SessionManager.session());
    }
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChildrenOf {
    
}
