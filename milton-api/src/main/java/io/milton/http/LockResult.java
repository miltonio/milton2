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

/**
 *
 */
public class LockResult implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public static LockResult failed( FailureReason failureReason) {
        return new LockResult(failureReason, null);
    }

    public static LockResult success(LockToken token) {
        return new LockResult(null, token);
    }

    public enum FailureReason {
        ALREADY_LOCKED(Response.Status.SC_CONFLICT),
        PRECONDITION_FAILED(Response.Status.SC_LOCKED);

        public Response.Status status;

        FailureReason(Response.Status status) {
            this.status = status;
        }
    }

    final FailureReason failureReason;
    final LockToken lockToken;

    public LockResult(FailureReason failureReason, LockToken lockToken) {
        this.failureReason = failureReason;
        this.lockToken = lockToken;
    }

    public boolean isSuccessful() {
        return failureReason == null;
    }

    public FailureReason getFailureReason() {
        return failureReason;
    }

    public LockToken getLockToken() {
        return lockToken;
    }

    
}
