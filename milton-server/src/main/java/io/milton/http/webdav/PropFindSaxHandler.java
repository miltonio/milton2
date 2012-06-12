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

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PropFindSaxHandler extends DefaultHandler {

    private static final Logger log = LoggerFactory.getLogger( PropFindSaxHandler.class );

    private Stack<QName> elementPath = new Stack<QName>();
    private Map<QName, String> attributes = new HashMap<QName, String>();
    private StringBuilder sb = new StringBuilder();
    private boolean inProp;
    private boolean allProp;

    @Override
    public void startElement( String uri, String localName, String name, Attributes attributes ) throws SAXException {
        if( elementPath.size() > 0 ) {
            String elname = elementPath.peek().getLocalPart();
            if( elname.equals( "prop" ) ) {
                inProp = true;
            }
        }
        if( localName.equals( "allprop" ) ) {
            allProp = true;
        }

        QName qname = new QName( uri, localName );
        elementPath.push( qname );
        super.startElement( uri, localName, name, attributes );
    }

    @Override
    public void characters( char[] ch, int start, int length ) throws SAXException {
        if( inProp ) {
            sb.append( ch, start, length );
        }
    }

    @Override
    public void endElement( String uri, String localName, String name ) throws SAXException {
        elementPath.pop();
        if( elementPath.size() > 0 && elementPath.peek().getLocalPart().endsWith( "prop" ) ) {
            if( sb != null ) {
//                uri = uri.substring( 0, uri.length()-1); // need to strip trailing :
                QName qname = new QName( uri, localName );
                getAttributes().put( qname, sb.toString().trim() );
            }
            sb.delete( 0, sb.length() );
        }

        super.endElement( uri, localName, name );
    }

    public Map<QName, String> getAttributes() {
        return attributes;
    }

    public boolean isAllProp() {
        return allProp;
    }
}
