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

package io.milton.http;

import io.milton.http.LockInfo.LockDepth;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import io.milton.http.LockInfo.LockScope;
import io.milton.http.LockInfo.LockType;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class LockInfoSaxHandler extends DefaultHandler {

    private static final Logger log = LoggerFactory.getLogger( LockInfo.class );

    public static LockInfo parseLockInfo( Request request ) throws IOException, FileNotFoundException, SAXException {
        InputStream in = request.getInputStream();

        XMLReader reader = XMLReaderFactory.createXMLReader();
        LockInfoSaxHandler handler = new LockInfoSaxHandler();
        reader.setContentHandler( handler );
        reader.parse( new InputSource( in ) );
        LockInfo info = handler.getInfo();
        info.depth = LockDepth.INFINITY; // todo
        info.lockedByUser = null;
        if( request.getAuthorization() != null ) {
            info.lockedByUser = request.getAuthorization().getUser();
        }
        if( info.lockedByUser == null ) {
            log.warn( "resource is being locked with a null user. This won't really be locked at all..." );
        }
        log.debug( "parsed lock info: " + info );
        return info;
    }	
	
    private LockInfo info = new LockInfo();
    private StringBuilder owner;
    private Stack<String> elementPath = new Stack<String>();

    @Override
    public void startElement( String uri, String localName, String name, Attributes attributes ) throws SAXException {
        elementPath.push( localName );
        if( localName.equals( "owner" ) ) {
            owner = new StringBuilder();
        }
        super.startElement( uri, localName, name, attributes );
    }

    @Override
    public void characters( char[] ch, int start, int length ) throws SAXException {
        if( owner != null ) {
            owner.append( ch, start, length );
        }
    }

    @Override
    public void endElement( String uri, String localName, String name ) throws SAXException {
        elementPath.pop();
        if( localName.equals( "owner" ) ) {
            log.debug( "owner: " + owner.toString());
            getInfo().lockedByUser = owner.toString();
        }
        if( elementPath.size() > 1 ) {
            if( elementPath.get( 1 ).equals( "lockscope" ) ) {
                if( localName.equals( "exclusive" ) ) {
                    getInfo().scope = LockScope.EXCLUSIVE;
                } else if( localName.equals( "shared" ) ) {
                    getInfo().scope = LockScope.SHARED;
                } else {
                    getInfo().scope = LockScope.NONE;
                }
            } else if( elementPath.get( 1 ).equals( "locktype" ) ) {
                if( localName.equals( "read" ) ) {
                    getInfo().type = LockType.READ;
                } else if( localName.equals( "write" ) ) {
                    getInfo().type = LockType.WRITE;
                } else {
                    getInfo().type = LockType.WRITE;
                }
            }

        }
        super.endElement( uri, localName, name );
    }

    public LockInfo getInfo() {
        return info;
    }
}
