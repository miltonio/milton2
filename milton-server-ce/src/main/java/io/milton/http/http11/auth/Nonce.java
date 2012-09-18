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
