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
