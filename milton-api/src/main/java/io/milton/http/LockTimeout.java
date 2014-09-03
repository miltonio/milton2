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

import io.milton.http.Request;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockTimeout implements Serializable{

    private static final long serialVersionUID = 1L;


    private static Logger log = LoggerFactory.getLogger(LockTimeout.class);
    private static final String INFINITE = "Infinite";
    
            
    public static LockTimeout parseTimeout(Request request) {
        String sTimeout = request.getTimeoutHeader();
        log.debug("..requested timeout: " + sTimeout);
        return parseTimeout(sTimeout);
    }
    
    public static LockTimeout parseTimeout(String s) {      
        if ( s==null ) return new LockTimeout((List<Long>)null);
        s = s.trim();
        if( s.length() == 0 ) return new LockTimeout((List<Long>)null);

        List<Long> list = new ArrayList<Long>();
        for( String part : s.split(",")) {
            part = part.trim();
            if( part.equalsIgnoreCase(INFINITE)) {
                list.add(Long.MAX_VALUE);
            } else {
                Long seconds = parseTimeoutPart(part);
                if(seconds != null ) {
                    list.add(seconds);
                }
            }
        }
        
        LockTimeout timeout = new LockTimeout(list);
        return timeout;
    }

    static String trim(String s) {
        if( s == null ) return "";
        return s.trim();
    }
    
    static boolean isPresent(String s) {
        return s != null && s.length()>0;
    }

    private static Long parseTimeoutPart(String part) {
        if( part == null || part.length() == 0 ) return null;
        int pos = part.indexOf("-");
        if( pos <= 0 ) {
            return null;
        }
        String s = part.substring(pos+1, part.length());
        long l = 0;
        try {
            l = Long.parseLong(s);
            return l;
        } catch (NumberFormatException numberFormatException) {
            log.error("Number format exception parsing timeout: " + s);
            return null;
        }        
    }
    
    final Long seconds;
    final Long[] otherSeconds;

    /**
     * 
     * 
     * @param timeout - duration that the lock will live for, in seconds
     */
    public LockTimeout(Long timeout) {
        this.seconds = timeout;
        this.otherSeconds = null;
    }
    
    private LockTimeout(List<Long> timeouts) {
        if( timeouts == null || timeouts.isEmpty() ) {
            this.seconds = null;
            this.otherSeconds = null;
        } else {
            this.seconds = timeouts.get(0);
            timeouts.remove(0);
            otherSeconds = new Long[timeouts.size()];
            timeouts.toArray(otherSeconds);
        }
    }
    
    /**
     * 
     * @return - the preferred timeout. Infinite is represents as Long.MAX_VALUE. Maybe null if no timeout provided
     */
    public Long getSeconds() {
        return seconds;
    }

    /**
     * 
     * @return - an array of less preferred timeouts
     */
    public Long[] getOtherSeconds() {
        return otherSeconds;
    }

    @Override
    public String toString() {
        if( this.seconds == null ) {
            return INFINITE;
        } else if(this.seconds.equals( Long.MAX_VALUE )) {
            return INFINITE;
        } else {
            return "Second-" + this.seconds;
        }
    }


    

    /**
     * Returns a current object which holds the expected end date/time, based
     * on defaultSeconds and maxSeconds, as well as the actual seconds used
     * in that calculation.
     *
     * This is handy for locking because we generally want to lock a resource
     * until a specific date/time, but we also want to report back the timeout
     * actually locked in terms of seconds
     * 
     * @return - the current time + getSeconds()
     */
    public DateAndSeconds getLockedUntil(Long defaultSeconds, Long maxSeconds) {
        Long l = getSeconds();        
        if( l == null ) {
            if( defaultSeconds != null ) {
                return addSeconds(defaultSeconds);
            } else if( maxSeconds != null ) {
                return addSeconds( maxSeconds);
            } else {
                return addSeconds(60l); // default default
            }
        } else {
            if( maxSeconds != null ) {
                if( getSeconds() > maxSeconds ) {
                    return addSeconds(maxSeconds);
                } else {
                    return addSeconds(l);
                }
            } else {
                return addSeconds(l);
            }
        }
    }
    
    public static DateAndSeconds addSeconds( Long l) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        int secs = (int)l.longValue();
        cal.add(Calendar.SECOND, secs);
        DateAndSeconds das = new DateAndSeconds();
        das.date = cal.getTime();        
        das.seconds = l;
        return das;
    }
        
    public static class DateAndSeconds {
        public Date date;
        public Long seconds;
    }
}
