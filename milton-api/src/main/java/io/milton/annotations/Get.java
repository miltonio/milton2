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
 * Marks a method as one which generates content for the given resource
 * 
 * The method return type can be
 *  - void, in which case you must write content to the outputStream in the method call
 *  - a byte[] which is written to the output
 *  - an InputStream, which will be written to output, then closed (in a try/finally block)
 *  - a String, which is interpreted as a template name. The ViewResolver is used to locate a template with the
 * given name, and it is invoked with a model object with name "resource"
 *  - a io.milton.common.ModelAndView which is passed to the ViewResolver to execute the template
 *  Any other return type causes an exception
 * 
 * The input parameters must be:
 *  - first must be the source object
 *  - then any of Request, Response, Range, contentType (String), parameters (Map), OutputStream
 *  
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Get {
    
}
