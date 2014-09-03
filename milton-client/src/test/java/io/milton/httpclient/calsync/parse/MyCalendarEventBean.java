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
