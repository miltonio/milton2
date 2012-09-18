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
