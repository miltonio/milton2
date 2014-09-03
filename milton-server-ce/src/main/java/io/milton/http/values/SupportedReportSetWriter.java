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
