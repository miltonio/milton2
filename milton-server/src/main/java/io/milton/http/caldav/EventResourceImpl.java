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

package io.milton.http.caldav;

import java.util.Date;

/**
 *
 * @author brad
 */
public class EventResourceImpl implements EventResource {
    private String uniqueId;
    private Date start;
    private Date end;
    private String summary;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    
    
    public Date getStart() {
        return start;
    }

    public void setStart(Date d) {
        this.start = d;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date d) {
        this.end = d;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String s) {
        this.summary = s;
    }

}
