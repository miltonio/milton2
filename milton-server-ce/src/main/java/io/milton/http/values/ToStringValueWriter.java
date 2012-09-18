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

import io.milton.common.Utils;
import io.milton.http.XmlWriter;
import java.util.Map;

public class ToStringValueWriter implements ValueWriter {

	@Override
    public boolean supports( String nsUri, String localName, Class c ) {
        return true;
    }

    private String nameEncode( String s ) {
        //return Utils.encode(href, false); // see MIL-31
		
		// Must not escape quotes because they're used in the getetag property
        //return Utils.escapeXml( s );
//		s = s.replaceAll("\"", "&quot;");
		s = s.replaceAll("&", "&amp;");
//		s = s.replaceAll("'", "&apos;");
		s = s.replaceAll("<", "&lt;");
		s = s.replaceAll(">", "&gt;");		
		return s;
	}

	@Override
    public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
        if( val == null ) {
            writer.writeProperty( prefix, localName );
        } else {
            String s = nameEncode( val.toString() );
            writer.writeProperty( prefix, localName, s );
        }
    }

	@Override
    public Object parse( String namespaceURI, String localPart, String value ) {
        return value;
    }
}
