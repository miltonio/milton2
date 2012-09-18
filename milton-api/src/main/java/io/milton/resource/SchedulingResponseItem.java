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

package io.milton.resource;

import io.milton.http.caldav.ITip;
import io.milton.http.caldav.ITip.StatusResponse;

/**
 *
 * @author brad
 */
public class SchedulingResponseItem {
    // Eg mailto:wilfredo@example.com
    private String recipient;

    private ITip.StatusResponse status;

    private String iCalText;

    public SchedulingResponseItem(String recipient, StatusResponse status, String iCalText) {
        this.recipient = recipient;
        this.status = status;
        this.iCalText = iCalText;
    }

    public SchedulingResponseItem() {
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public StatusResponse getStatus() {
        return status;
    }

    public void setStatus(StatusResponse status) {
        this.status = status;
    }

    public String getiCalText() {
        return iCalText;
    }

    public void setiCalText(String iCalText) {
        this.iCalText = iCalText;
    }
}
