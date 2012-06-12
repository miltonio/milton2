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
