/*
 * Copyright (C) 2012 McEvoy Software Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.bandstand.util;

import java.text.ParseException;
import java.util.Date;
import net.fortuna.ical4j.model.TimeZone;

/**
 *
 * @author brad
 */
public class CalUtils {

    public static  net.fortuna.ical4j.model.Date toCalDate(Date dt, TimeZone timezone) {
        try {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeZone(timezone);
            cal.setTime(dt);
            net.fortuna.ical4j.model.Date start = new net.fortuna.ical4j.model.Date(toCalDate(cal));
            return start;
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static  net.fortuna.ical4j.model.DateTime toCalDateTime(Date dt, TimeZone timezone) {
        if( dt == null ) {
            return null;
        }
        try {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeZone(timezone);
            cal.setTime(dt);
            net.fortuna.ical4j.model.DateTime start = new net.fortuna.ical4j.model.DateTime(toCalDateTime(cal));
            return start;
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static  String toCalDate(java.util.Calendar cal) {
        String s = "";
        s += cal.get(java.util.Calendar.YEAR);
        s += pad2(cal.get(java.util.Calendar.MONTH) + 1);
        s += pad2(cal.get(java.util.Calendar.DAY_OF_MONTH));
        return s;
    }

    public static  String toCalDateTime(java.util.Calendar cal) {
        String s = toCalDate(cal);
        s += "T";
        s += pad2(cal.get(java.util.Calendar.HOUR_OF_DAY));
        s += pad2(cal.get(java.util.Calendar.MINUTE));
        s += pad2(cal.get(java.util.Calendar.SECOND));
//        s += "Z";
        return s;
        //"20100101T070000Z";
    }

    public static  String pad2(int i) {
        if (i > 9) {
            return i + "";
        } else {
            return "0" + i;
        }
    }    
}
