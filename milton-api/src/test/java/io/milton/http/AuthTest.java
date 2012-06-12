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
