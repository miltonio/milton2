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
