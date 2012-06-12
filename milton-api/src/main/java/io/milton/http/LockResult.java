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

import io.milton.http.Response;

/**
 *
 */
public class LockResult {

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
