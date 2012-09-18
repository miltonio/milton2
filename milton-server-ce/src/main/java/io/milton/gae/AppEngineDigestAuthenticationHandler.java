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

package io.milton.gae;

import io.milton.http.http11.auth.DigestAuthenticationHandler;


/**
 * Convenience subclass of DigestAuthenticationHandler which just creates
 * a AppEngineMemcacheNonceProvider as the default nonce provider.
 *
 * Note that if you want to configure the AppEngineMemcacheNonceProvider you
 * should just create a DigestAuthenticationHandler and pass it the configured
 * instance of AppEngineMemcacheNonceProvider.
 *
 * @author Scott Hernandez
 */
public class AppEngineDigestAuthenticationHandler extends DigestAuthenticationHandler {
	public AppEngineDigestAuthenticationHandler(){
		super(new AppEngineMemcacheNonceProvider(3600));
	}
}
