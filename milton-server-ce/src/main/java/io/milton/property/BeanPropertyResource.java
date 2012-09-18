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

package io.milton.property;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation type to identify classes to be accessible by
 * BeanPropertySource
 *
 * This allows them to have their properties read from and written to
 * by PROPFIND and PROPPATCH.
 *
 * @author brad
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BeanPropertyResource {

    /**
     * Default property which is the namespace uri for the properties
     * on this resource
     *
     * E.g. http://mycompany.com/ns/example
     * 
     * @return - the namespace uri
     */
    String value();
    /**
     *
     * @return - true allows the resource to be updatable
     */
    boolean writable() default true;

    /**
     * If true, indicates that properties on the resource should be accessible
     * unless otherwise specified
     * 
     * @return
     */
    boolean enableByDefault() default true;
}
