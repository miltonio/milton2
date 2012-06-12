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
import io.milton.http.XmlWriter.Element;
import io.milton.http.webdav.WebDavProtocol;
import java.util.Map;

/**
 *
 * @author alex
 */
public class SupportedReportSetWriter  implements ValueWriter {

	@Override
    public boolean supports( String nsUri, String localName, Class c ) {
        return SupportedReportSetList.class.isAssignableFrom( c );
    }

	@Override
    public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
        
      
        SupportedReportSetList list = (SupportedReportSetList) val;
        Element reportSet = writer.begin( WebDavProtocol.DAV_PREFIX + ":supported-report-set" ).open();
        if( list != null ) {
            for( String s : list) {
                Element supportedReport = writer.begin( WebDavProtocol.DAV_PREFIX + ":supported-report" ).open();
                Element report = writer.begin( WebDavProtocol.DAV_PREFIX + ":report" ).open();
                writer.writeProperty( WebDavProtocol.DAV_PREFIX + ":" + s );
                report.close();
                supportedReport.close();
            }
        }
        reportSet.close();
    }

	@Override
    public Object parse( String namespaceURI, String localPart, String value ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
