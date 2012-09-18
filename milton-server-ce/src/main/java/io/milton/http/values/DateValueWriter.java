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

package io.milton.http.values;

import io.milton.http.DateUtils;
import io.milton.http.DateUtils.DateParseException;
import io.milton.http.XmlWriter;
import java.util.Date;
import java.util.Map;

public class DateValueWriter implements ValueWriter {

	@Override
    public boolean supports( String nsUri, String localName, Class c ) {
        return Date.class.isAssignableFrom(c);
    }

	@Override
    public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
        if( val == null ) {
            writer.writeProperty( prefix, localName );
        } else {
            Date date = (Date) val;
            String s = DateUtils.formatDate( date );
            writer.writeProperty( prefix, localName, s );
        }
    }

	@Override
    public Object parse( String namespaceURI, String localPart, String value ) {
        if( value == null || value.length() == 0 ) return null;
        Date dt;
        try {
            dt = DateUtils.parseDate( value );
            return dt;
        } catch( DateParseException ex ) {
            throw new RuntimeException( value, ex );
        }
    }
}
