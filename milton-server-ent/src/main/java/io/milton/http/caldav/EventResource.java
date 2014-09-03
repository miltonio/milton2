/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */

package io.milton.http.caldav;

import java.util.Date;

/**
 * An optional interface to represent a VEVENT item
 *
 * Only use this if you want to use ICalFormatter
 *
 * @author brad
 */
public interface EventResource {

    String getUniqueId();
    
    Date getStart();

    void setStart( Date d );

    Date getEnd();

    void setEnd( Date d );

    String getSummary();

    void setSummary( String s );
}
