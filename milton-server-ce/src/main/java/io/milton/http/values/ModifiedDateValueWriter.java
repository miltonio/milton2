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
import io.milton.http.XmlWriter;
import io.milton.http.webdav.WebDavProtocol;
import java.util.Date;
import java.util.Map;

/**
 * Windows explorer is VERY picky about the format of its modified date, which
 * this class supports
 *
 * Only applies to the getlastmodified field
 *
 * @author brad
 */
public class ModifiedDateValueWriter implements ValueWriter {

    public boolean supports( String nsUri, String localName, Class c ) {
        return nsUri.equals( WebDavProtocol.NS_DAV.getName() ) && localName.equals( "getlastmodified" );
    }

    public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
        //sendDateProp(xmlWriter, "D:" + fieldName(), res.getModifiedDate());
        Date dt = (Date) val;
        String f;
        if( dt == null ) {
            f = "";
        } else {
            f = DateUtils.formatForWebDavModifiedDate( dt );
        }
        writer.writeProperty( prefix, localName, f );
    }

    public Object parse( String namespaceURI, String localPart, String value ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
