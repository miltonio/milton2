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

import io.milton.http.LockInfo;
import io.milton.http.LockInfo.LockScope;
import io.milton.http.LockInfo.LockType;
import io.milton.common.Utils;
import io.milton.http.XmlWriter;
import java.net.URI;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class LockWriterHelper {

    private static final Logger log = LoggerFactory.getLogger( LockWriterHelper.class );

	private static String D = WebDavProtocol.DAV_PREFIX;
	
    private boolean stripHrefOnOwner = true;

    public void appendDepth( XmlWriter writer, LockInfo.LockDepth depthType ) {
        String s = "Infinity";
        if( depthType != null ) {
            if( depthType.equals( LockInfo.LockDepth.INFINITY ) )
                s = depthType.name().toUpperCase();
        }
        writer.writeProperty( null, D + ":depth", s );

    }

    public void appendOwner( XmlWriter writer, String owner ) {
        boolean validHref;
        if( owner == null ) {
            log.warn( "owner is null");
            validHref = false;
        } else {
            validHref = isValidHref( owner );
        }
        log.debug( "appendOwner: " + validHref + " - " + stripHrefOnOwner);
        if( !validHref && stripHrefOnOwner ) { // BM: reversed login on validHref - presumably only write href tag for href values???
            writer.writeProperty( null, D + ":owner", owner );
        } else {
            XmlWriter.Element el = writer.begin( D + ":owner" ).open();
            XmlWriter.Element el2 = writer.begin( D + ":href" ).open();
            if( owner != null ) {
                el2.writeText( owner );
            }
            el2.close();
            el.close();
        }        
    }

    public void appendScope( XmlWriter writer, LockScope scope ) {
        writer.writeProperty( null, D + ":lockscope", "<" + D + ":" + scope.toString().toLowerCase() + "/>" );
    }

    /**
     * Sets the timeout in seconds, with a maximum as required by the spec. See http://jira.ettrema.com:8080/browse/MIL-89
     *
     * RFC4918 (14.29 timeout XML Element; 10.7 Timeout Request Header) states:
     * "The timeout value for TimeType "Second" MUST NOT be greater than 232-1."
     * 2^32 - 1 = 4 294 967 295 (136 years)
     * http://greenbytes.de/tech/webdav/rfc4918.html#HEADER_Timeout
     *
     * @param writer
     * @param seconds
     */
    public void appendTimeout( XmlWriter writer, Long seconds ) {
        if( seconds != null && seconds > 0 ) {
            writer.writeProperty( null, D + ":timeout", "Second-" + Utils.withMax(seconds, 4294967295l) );
        }
    }

    public void appendTokenId( XmlWriter writer, String tokenId ) {
        XmlWriter.Element el = writer.begin( D + ":locktoken" ).open();
        writer.writeProperty( null, D + ":href", "opaquelocktoken:" + tokenId );
        el.close();
    }

    public void appendType( XmlWriter writer, LockType type ) {
        writer.writeProperty( null, D + ":locktype", "<" + D + ":" + type.toString().toLowerCase() + "/>" );
    }

    public void appendRoot( XmlWriter writer, String lockRoot ) {
        XmlWriter.Element el = writer.begin( D + ":lockroot" ).open();
        writer.writeProperty( null, D + ":href", lockRoot );
        el.close();
    }

    /**
     * If set the owner value will not be wrapped in an href tag unless it is
     * a valid URL.
     * E.g. true: this -> <owner>this</owner>
     *    false: that -> <owner><href>that</href></owner>
     *
     * See also LockTokenValueWriter.java
     *
     * @return
     */
    public boolean isStripHrefOnOwner() {
        return stripHrefOnOwner;
    }

    public void setStripHrefOnOwner( boolean stripHrefOnOwner ) {
        this.stripHrefOnOwner = stripHrefOnOwner;
    }

    private boolean isValidHref( String owner ) {
        log.debug( "isValidHref: " + owner);
        if(owner.startsWith( "http")) {
            try {
                URI u = new URI( owner );
                log.debug( "uri: " + u);
                return true;
            } catch( URISyntaxException ex ) {
                log.debug( "ex: " + ex);
                return false;
            }
        } else {
            return false;
        }
    }
}
