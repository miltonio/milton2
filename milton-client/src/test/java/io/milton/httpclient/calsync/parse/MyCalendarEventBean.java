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
package io.milton.httpclient.calsync.parse;

import io.milton.httpclient.annotation.Etag;
import io.milton.httpclient.calsync.parse.annotation.Description;
import io.milton.httpclient.calsync.parse.annotation.EndDate;
import io.milton.httpclient.annotation.Name;
import io.milton.httpclient.calsync.parse.annotation.Location;
import io.milton.httpclient.calsync.parse.annotation.Organizer;
import io.milton.httpclient.calsync.parse.annotation.StartDate;
import io.milton.httpclient.calsync.parse.annotation.Summary;
import io.milton.httpclient.calsync.parse.annotation.Timezone;
import io.milton.httpclient.calsync.parse.annotation.Uid;
import java.util.Date;

/**
 *
 * @author brad
 */
public class MyCalendarEventBean {
    private String name;
    private String uid;
    private String etag;
    private Date startDate;   
    private Date endDate;    
    private String timezone;    
    private String summary;    
    private String description;    
    private String location;
    private String organizer;

    @Uid
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Etag
    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    @Name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @EndDate
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @StartDate
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Summary
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Timezone
    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @Location
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Organizer
    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }
    
    
    
    
}
