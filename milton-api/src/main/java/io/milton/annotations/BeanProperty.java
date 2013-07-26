package io.milton.annotations;

import io.milton.resource.AccessControlledResource.Priviledge;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation type to identify properties to be accessible by
 * BeanPropertySource
 *
 * This allows them to have their properties read from and written to
 * by PROPFIND and PROPPATCH.
 *
 * Note that to implement validation rules with feedback to the user you
 * can throw a PropertySetException from within your setters.
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
