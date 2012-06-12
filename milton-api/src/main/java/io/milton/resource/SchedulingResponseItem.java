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
