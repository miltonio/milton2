/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.milton.property;

import io.milton.resource.Resource;
import io.milton.http.Response;
import io.milton.http.Response.Status;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * Defines a source of properties. This is used by both propfind and proppatch
 * 
 *
 * @author brad
 */
public interface PropertySource {

    public enum PropertyAccessibility {

        UNKNOWN,
        READ_ONLY,
        WRITABLE
    }

    public static class PropertyMetaData {

        private final PropertyAccessibility accessibility;
        private final Class valueType;
        public static final PropertyMetaData UNKNOWN = new PropertyMetaData( PropertyAccessibility.UNKNOWN, null );

        public PropertyMetaData( PropertyAccessibility accessibility, Class valueType ) {
            this.accessibility = accessibility;
            this.valueType = valueType;
        }

        public PropertyAccessibility getAccessibility() {
            return accessibility;
        }

        public Class getValueType() {
            return valueType;
        }

        public boolean isUnknown() {
            return accessibility.equals( PropertyAccessibility.UNKNOWN );
        }

        public boolean isWritable() {
            return accessibility.equals( PropertyAccessibility.WRITABLE );
        }
    }

	/**
	 * Return the typed value of the given property. For example, if the property
	 * is a date/time value it should generally return a java.util.Date. Milton
	 * will take care of formatting types values into XML responses.
	 * 
	 * If the current user cannot read the requested property throw a NotAuthorizedException,
	 * this will set the appropriate status against this 
	 * 
	 * @param name
	 * @param r
	 * @return
	 * @throws NotAuthorizedException 
	 */
    Object getProperty( QName name, Resource r ) throws NotAuthorizedException;

	/**
	 * Update the given property to the value given. If the value is invalid
	 * throw a PropertySetException, and if the user is not permitted
	 * to update the property throw a NotAuthorizedException
	 * 
	 * @param name
	 * @param value
	 * @param r
	 * @throws com.bradmcevoy.property.PropertySource.PropertySetException
	 * @throws NotAuthorizedException 
	 */
    void setProperty( QName name, Object value, Resource r ) throws PropertySetException, NotAuthorizedException;

    /**
     * Check to see if the property is known, and if it is writable.
     *
     * The returned value also contains a class which is the most specific known
     * class of the values which can be contained in this property. This class
     * must be sufficient to locate a ValueWriter to parse the textual representation
     * sent in PROPPATCH requests.
     *
     * @param name - the qualified name of the property
     * @param r - the resource which might contain the property
     * @return - never null, contains an enum value indicating if the property is known
     * to this source, and if it is writable, and a class indicating the type of the property.
     */
    PropertyMetaData getPropertyMetaData( QName name, Resource r )throws NotAuthorizedException, BadRequestException;

    /**
     * Remove the given property. There may be a semantic difference in some
     * cases between setting a property to a null value vs removing the property.
     * Generally this should completely the remove the property if possible.
     *
     * @param name
     * @param r
     */
    void clearProperty( QName name, Resource r ) throws PropertySetException, NotAuthorizedException;

    /**
     *
     * @param r - the resource which may contain properties
     * 
     * @return - all properties known by this source on the given resource.
     * This list should be exclusive. Ie only return properties not returned
     * by any other source
     */
    List<QName> getAllPropertyNames( Resource r ) throws NotAuthorizedException, BadRequestException;

    /**
     * Exception from setting a field
     */
    public class PropertySetException extends RuntimeException {

        private static final long serialVersionUID = 1L;
        private Response.Status status;
        private String notes;

        public PropertySetException( Status status, String notes ) {
            this.status = status;
            this.notes = notes;
        }

        /**
         * A response code indicating the problem. Eg 500 for a processing error,
         * or conflict, forbidden, etc
         *
         * @return
         */
        public Status getStatus() {
            return status;
        }

        /**
         * Should contain a human readable description of the problem
         *
         * @return
         */
        public String getErrorNotes() {
            return notes;
        }
    }
}
