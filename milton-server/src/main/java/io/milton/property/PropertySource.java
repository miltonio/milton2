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
