/*
 * Copyright 2012 McEvoy Software Ltd.
 *
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

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    
    
    @Override
    public Date getStart() {
        return start;
    }

    @Override
    public void setStart(Date d) {
        this.start = d;
    }

    @Override
    public Date getEnd() {
        return end;
    }

    @Override
    public void setEnd(Date d) {
        this.end = d;
    }

    @Override
    public String getSummary() {
        return summary;
    }

    @Override
    public void setSummary(String s) {
        this.summary = s;
    }

}
