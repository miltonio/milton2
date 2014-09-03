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


package io.milton.http;

import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class AuthTest extends TestCase {
    
    public void testBasic() {
        Auth auth = new Auth( "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");
        //Auth auth = new Auth( "Basic username=Aladdin,password=\"open sesame\"");
        assertEquals( "Aladdin", auth.getUser());
        assertEquals( "open sesame", auth.getPassword());
    }

    public void testDigest() {
        Auth auth = new Auth( "Digest username=\"Mufasa\",realm=\"testrealm@host.com\",nonce=\"ZTMyNmFmNDEtYWEwYy00MTc5LTk2OWEtZjMyOGRiOWI1NTg0\",uri=\"/webdav/secure\",cnonce=\"09683d5720f7e5e7dec2daeee585fe15\",nc=00000001,response=\"e6e7559f052bf75cdd8a979943197f40\",qop=\"auth\"");
        assertEquals( "Mufasa", auth.getUser());
        assertEquals( "testrealm@host.com", auth.getRealm());
        assertEquals( "ZTMyNmFmNDEtYWEwYy00MTc5LTk2OWEtZjMyOGRiOWI1NTg0", auth.getNonce());
        assertEquals( "/webdav/secure", auth.getUri());
        assertEquals( "09683d5720f7e5e7dec2daeee585fe15", auth.getCnonce());
        assertEquals( "e6e7559f052bf75cdd8a979943197f40", auth.getResponseDigest());
        assertEquals( "auth", auth.getQop());


    }
}
