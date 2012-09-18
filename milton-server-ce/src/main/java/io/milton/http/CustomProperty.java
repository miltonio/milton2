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
