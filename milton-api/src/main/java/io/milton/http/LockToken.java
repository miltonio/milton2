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

import java.io.Serializable;
import java.util.Date;

public class LockToken implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * the date/time that this lock was created or last refreshed
     */
    private Date from;
    public String tokenId;
    public LockInfo info;
    public LockTimeout timeout;

    public LockToken() {
        from = new Date();
    }

    public LockToken(String tokenId, LockInfo info, LockTimeout timeout) {
        from = new Date();
        this.tokenId = tokenId;
        this.info = info;
        this.timeout = timeout;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public boolean isExpired() {
        long secondsDif = dateDiffSeconds(new Date(), from);
        // http://jira.ettrema.com:8080/browse/MIL-79
        Long seconds = timeout.getSeconds();
        if (seconds == null) // Infinite
        {
            return false;
        }
        return (secondsDif > seconds);

    }

    private long dateDiffSeconds(Date dt1, Date dt2) {
        return (dt1.getTime() - dt2.getTime()) / 1000;

    }
}
