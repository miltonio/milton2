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

package io.milton.http;

/**
 * Represents support for a given property. The property may be null, blank
 * or have a value
 *
 */
public interface CustomProperty {

    /**
     * Returns a class which is assignable from any value which can be stored
     * in this property. This should be sufficient to determine a ValueWriter
     * to parse a PROPPATCH value.
     * 
     * @return
     */
    Class getValueClass();

    /**
     * Returns the typed value. It should be assumed that this value could
     * be serialised, although it doesnt require the Serializable interface
     *
     * @return
     */
    Object getTypedValue();

    /**
     * Returns a textual representation of the value suitable for consumption
     * by wedav clients, except that it should not be character encoded as
     * milton will do that
     *
     * @return
     */
    String getFormattedValue();

    /**
     * Set the unencoded string value into this property. This may include
     * parsing if this is a typed property.
     *
     * @param s
     */
    void setFormattedValue( String s );
}
