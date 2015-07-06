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

import io.milton.property.PropertySource.PropertySetException;
import io.milton.resource.AccessControlledResource.Priviledge;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation type to identify properties to be accessible by
 * {@link BeanPropertySource}
 *
 * <p>This allows them to have their properties read from and written to
 * by PROPFIND and PROPPATCH.
 *
 * <p>Note that to implement validation rules with feedback to the user you
 * can throw a {@link PropertySetException} from within your setters.
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BeanProperty {

    /**
     * Required role to read this property
     *
     * @return
     */
    Priviledge readRole() default Priviledge.READ;

    /**
     * Required role to change the property
     *
     * @return
     */
    Priviledge writeRole() default Priviledge.WRITE;
	
    /**
     * True indicates that the method should be enabled (ie DAV accessible)
     * regardless of the class default
     *
     * False indicats that the property is accessible if the class default is to allow access
     *
     * @return
     */	
	boolean value() default true;
}
