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

package io.milton.http.webdav2;

import io.milton.http.LockInfo;
import io.milton.http.LockToken;
import io.milton.http.XmlWriter;
import io.milton.http.XmlWriter.Element;
import io.milton.http.values.ValueWriter;
import io.milton.http.webdav.WebDavProtocol;
import java.util.Map;

public class LockTokenValueWriter implements ValueWriter {

    private LockWriterHelper lockWriterHelper = new LockWriterHelper();

    public LockWriterHelper getLockWriterHelper() {
        return lockWriterHelper;
    }

    public void setLockWriterHelper( LockWriterHelper lockWriterHelper ) {
        this.lockWriterHelper = lockWriterHelper;
    }

	@Override
    public boolean supports( String nsUri, String localName, Class c ) {
        return LockToken.class.isAssignableFrom( c );
    }

	@Override
    public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
        LockToken token = (LockToken) val;
		String d = WebDavProtocol.DAV_PREFIX;
        Element lockDiscovery = writer.begin( d + ":lockdiscovery" ).open();		
        if( token != null ) {
			Element activeLock = writer.begin( d + ":activelock" ).open();
            LockInfo info = token.info;
            lockWriterHelper.appendType( writer, info.type );
            lockWriterHelper.appendScope( writer, info.scope );
            lockWriterHelper.appendDepth( writer, info.depth );
            lockWriterHelper.appendOwner( writer, info.lockedByUser );
            lockWriterHelper.appendTimeout( writer, token.timeout.getSeconds() );
            lockWriterHelper.appendTokenId( writer, token.tokenId );
            lockWriterHelper.appendRoot( writer, href );
			activeLock.close();
        }		
        lockDiscovery.close();
    }

	@Override
    public Object parse( String namespaceURI, String localPart, String value ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
