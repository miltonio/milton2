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

package io.milton.httpclient;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.client.methods.HttpRequestBase;

/**
 *
 * @author mcevoyb
 */
public class UnLockMethod extends HttpRequestBase {

	private final String lockToken;
	
    public UnLockMethod( String uri, String lockToken ) throws URISyntaxException {
        setURI(new URI(uri));
		this.lockToken = lockToken;
		addHeader("Lock-Token", lockToken);
    }

    @Override
    public String getMethod() {
        return "UNLOCK";
    }

	public String getLockToken() {
		return lockToken;
	}
	
	
}
