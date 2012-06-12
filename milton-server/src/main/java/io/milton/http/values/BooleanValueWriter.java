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

package io.milton.http.values;

import io.milton.http.XmlWriter;
import java.util.Map;

public class BooleanValueWriter implements ValueWriter {

    public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
        Boolean b = (Boolean) val;
        writer.writeProperty( prefix, localName, b.toString().toUpperCase() );
    }

    public boolean supports( String nsUri, String localName, Class c ) {
        return c.equals( Boolean.class ) || c.equals(boolean.class);
    }

    public Object parse( String namespaceURI, String localPart, String value ) {
        if( value == null ) return false;
        value = value.toLowerCase();
        return value.equals( "t") || value.equals( "true");
    }
}
