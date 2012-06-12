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

package io.milton.http.webdav;

import io.milton.common.StreamUtils;
import java.io.ByteArrayInputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Simple implmentation which just parses the request body. If no xml is present
 * it will return an empty set.
 *
 * Note this generally shouldnt be used directly, but should be wrapped by
 * MSPropFindRequestFieldParser to support windows clients.
 *
 * @author brad
 */
public class DefaultPropFindRequestFieldParser implements PropFindRequestFieldParser {

    private static final Logger log = LoggerFactory.getLogger( DefaultPropFindRequestFieldParser.class );

    public DefaultPropFindRequestFieldParser() {
    }

	@Override
    public PropertiesRequest getRequestedFields( InputStream in ) {
        try {
            final Set<QName> set = new LinkedHashSet<QName>();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            StreamUtils.readTo( in, bout, false, true );
            byte[] arr = bout.toByteArray();
            if( arr.length > 1 ) {
                ByteArrayInputStream bin = new ByteArrayInputStream( arr );
                XMLReader reader = XMLReaderFactory.createXMLReader();
                PropFindSaxHandler handler = new PropFindSaxHandler();
                reader.setContentHandler( handler );
                try {
                    reader.parse( new InputSource( bin ) );
                    if( handler.isAllProp() ) {
                        return new PropertiesRequest();
                    } else {
                        set.addAll( handler.getAttributes().keySet() );
                    }
                } catch( IOException e ) {
                    log.warn( "exception parsing request body", e );
                    // ignore
                } catch( SAXException e ) {
                    log.warn( "exception parsing request body", e );
                    // ignore
                }
            }
            return PropertiesRequest.toProperties(set);
        } catch( Exception ex ) {
            throw new RuntimeException( ex );
        }
    }
}
