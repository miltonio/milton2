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

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Represents a Nonce which has been issued and is stored in memory
 *
 * @author brad
 */
public class Nonce implements Serializable{
    private static final long serialVersionUID = 1L;

    /**
     * The date it was issued.
     */
    private final UUID value;
    private final Date issued;
    private final long nonceCount;

    public Nonce( UUID value, Date issued ) {
        this.value = value;
        this.issued = issued;
        this.nonceCount = 0;
    }

    Nonce( UUID value, Date issued, long nonceCount ) {
        this.value = value;
        this.issued = issued;
        this.nonceCount = nonceCount;
    }

    public Nonce increaseNonceCount(long newNonceCount) {
//        if( newNonceCount <= this.nonceCount) throw new IllegalArgumentException( "new nonce-count is not greater then the last. old:" + nonceCount + " new:" + newNonceCount);
        return new Nonce( value, issued, newNonceCount);
    }

    /**
     * @return the value
     */
    public UUID getValue() {
        return value;
    }

    /**
     * @return the issued
     */
    public Date getIssued() {
        return issued;
    }

    public long getNonceCount() {
        return nonceCount;
    }


}
