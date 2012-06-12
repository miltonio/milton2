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

package io.milton.http.http11.auth;

import io.milton.http.Request;
import io.milton.resource.Resource;

/**
 * Provides a source of nonce values to be used in Digest authentication,
 * and a means to validate nonce values.
 *
 * Implementations should ensure that nonce values are available across all
 * servers in a cluster, and that they expire appropriately.
 *
 * Implementations should also ensure that nonce-count values are always
 * increasing, if provided.
 *
 * @author brad
 */
public interface NonceProvider {

   

    public enum NonceValidity {

        OK,
        EXPIRED,
        INVALID
    }

    /**
     * Check to see if the given nonce is known. If known, is it still valid
     * or has it expired.
     *
     * The request may also be considered invalid if the nonceCount value is
     * non-null and is not greater then any previous value for the valid nonce value.
     *
     * @param nonce - the nonce value given by a client to be checked.
     * @param nonceCount - may be null for non-auth requests. otherwise this should
     * be a monotonically increasing value. The server should record the previous
     * value and ensure that this value is greater then any previously given.
     * @return
     */
    NonceValidity getNonceValidity( String nonce, Long nonceCount );

    /**
     * Create and return a nonce value to be used for an authentication session.
     *
     *
     * @param resource - the resource being accessed.
     * @param request - the current request
     * @return - some string to be used as a nonce value.
     */
    String createNonce( Resource resource, Request request );
}
