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
