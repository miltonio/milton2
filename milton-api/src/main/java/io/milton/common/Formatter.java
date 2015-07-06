/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.milton.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handy functions exposes to rendering logic for formatting.
 *
 * @author brad
 */
public class Formatter {

    private static Logger log = LoggerFactory.getLogger(Formatter.class);
    
    public static final String CHECKBOX_SUFFIX = "_checkbox";
    
    public static ThreadLocal<DateFormat> tlSdfUkShort = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("dd/MM/yyyy");
        }
    };
    public static ThreadLocal<DateFormat> tlSdfUkLong = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("dd MMMM yyyy");
        }
    };
    public static final ThreadLocal<DateFormat> sdfDateOnly = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("dd/MM/yyyy");
        }
    };
    public static final ThreadLocal<DateFormat> sdfDateAndTime = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm");
        }
    };
    


    /**
     * Null safe method, returns empty string if the value is null
     *
     * @param o
     * @return
     */
    public String toString(Object o) {
        if (o == null) {
            return "";
        } else {
            return o.toString();
        }
    }

    public Boolean toBool(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof Boolean) {
            return (Boolean) o;
        } else if (o instanceof Integer) {
            Integer i = (Integer) o;
            return i == 0;
        } else if (o instanceof String) {
            String s = (String) o;
            s = s.toLowerCase();
            s = s.trim();
            if (s.length() > 0) {
                return s.equals("true") || s.equals("yes");
            } else {
                return null;
            }
        } else {
            throw new RuntimeException("Unsupported boolean type: " + o.getClass());
        }

    }

    public BigDecimal toDecimal(Object o, int places) {
        if (o == null) {
            return BigDecimal.ZERO;
        } else if (o instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) o;
            return bd.setScale(places, RoundingMode.HALF_UP);
        } else if (o instanceof Double) {
            Double d = (Double) o;
            return BigDecimal.valueOf(d).setScale(places, RoundingMode.HALF_UP);
        } else if (o instanceof Integer) {
            Integer i = (Integer) o;
            return BigDecimal.valueOf(i.longValue()).setScale(places, RoundingMode.HALF_UP);
        } else if (o instanceof Float) {
            Float f = (Float) o;
            return BigDecimal.valueOf(f.doubleValue()).setScale(places, RoundingMode.HALF_UP);
        } else if (o instanceof String) {
            String s = (String) o;
            s = s.trim();
            if (s.length() == 0) {
                return BigDecimal.ZERO;
            } else {
                try {
                    return new BigDecimal(s).setScale(places, RoundingMode.HALF_UP);
                } catch (NumberFormatException numberFormatException) {
                    throw new RuntimeException("Non-numeric data: " + s);
                }
            }
        } else {
            throw new RuntimeException("Unsupported value type, should be numeric: " + o.getClass());
        }
    }

    public Double toDouble(Object o) {
        if (o == null) {
            return 0d;
        } else if (o instanceof String) {
            String s = (String) o;
            s = s.trim();
            if (s.length() == 0) {
                return 0d;
            } else {
                try {
                    return Double.valueOf(s);
                } catch (NumberFormatException numberFormatException) {
                    throw new RuntimeException("Non-numeric data: " + s);
                }
            }
        } else if (o instanceof Double) {
            return (Double) o;
        } else if (o instanceof Integer) {
            Integer i = (Integer) o;
            return (double) i;
        } else if (o instanceof Float) {
            Float f = (Float) o;
            return f.doubleValue();
        } else if (o instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) o;
            return bd.doubleValue();
        } else {
            throw new RuntimeException("Unsupported value type, should be numeric: " + o.getClass());
        }
    }

    public Long toLong(Object oLimit) {
        return toLong(oLimit, false);
    }

    public Long toLong(Object oVal, boolean withNulls) {
        Long limit;
        if (oVal == null) {
            limit = withNulls ? null : 0l;
        } else if (oVal instanceof Long) {
            limit = (Long) oVal;
        } else if (oVal instanceof Integer) {
            int i = (Integer) oVal;
            limit = (long) i;
        } else if (oVal instanceof Double) {
            Double d = (Double) oVal;
            return d.longValue();
        } else if (oVal instanceof Float) {
            Float d = (Float) oVal;
            return d.longValue();
        } else if (oVal instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) oVal;
            return bd.longValue();
        } else if (oVal instanceof Boolean) {
            Boolean bb = (Boolean) oVal;
            return bb ? 1l : 0l;
        } else if (oVal instanceof String) {
            String s = (String) oVal;
            if (s.length() == 0) {
                limit = withNulls ? null : 0l;
            } else {
                if (s.equals("true") || s.equals("false")) {
                    Boolean b = Boolean.parseBoolean(s);
                    return toLong(b);
                } else {
                    if (s.contains(".")) {
                        Double d = toDouble(s);
                        limit = d.longValue();
                    } else {
                        limit = Long.parseLong(s);
                    }
                }
            }
        } else {
            throw new RuntimeException("unsupported class: " + oVal.getClass());
        }
        return limit;
    }

    public int getYear(Object o) {
        if (o == null || !(o instanceof Date)) {
            return 0;
        }
        Date dt = (Date) o;

        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.get(Calendar.YEAR);
    }

    public int getMonth(Object o) {
        if (o == null || !(o instanceof Date)) {
            return 0;
        }
        Date dt = (Date) o;

        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.get(Calendar.MONTH) + 1;
    }

    public int getDayOfMonth(Object o) {
        if (o == null || !(o instanceof Date)) {
            return 0;
        }
        Date dt = (Date) o;

        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.get(Calendar.DAY_OF_MONTH) + 1;
    }



    public String formatMinsAsDuration(Object o) {
        return formatMinsAsDuration(o, true);
    }

    /**
     * Given a value which can be parsed to a Long, return it formatted as a
     * human readable duration such as 12:30 (12 mins, 30 seconds) or 12 mins, 3
     * hrs 20
     *
     * @param o
     * @param numeric
     * @return
     */
    public String formatMinsAsDuration(Object o, boolean numeric) {
        Long l = toLong(o);
        if (l == null) {
            return "";
        } else {
            if (l == 0) {
                return "";
            }
            long hours = l / 60;
            long mins = l % 60;
            if (numeric) {
                return hours + ":" + pad(mins, 2);
            } else {
                if (hours == 0) {
                    return mins + "mins";
                } else if (hours == 1) {
                    return hours + "hr " + mins;
                } else {
                    return hours + "hrs " + mins;
                }
            }
        }
    }

    public String pad2(long l) {
        return pad(l, 2);
    }

    public String pad(long l, int length) {
        return padWith("0", l, length);
    }

    public String padWith(String padChar, long l, int length) {
        return _pad(padChar, l + "", length);
    }

    private String _pad(String padChar, String val, int length) {
        if (val.length() >= length) {
            return val;
        }
        return _pad(padChar, padChar + val, length);
    }

    /**
     * Format as a percentage, including a percentage symbol and where
     * blank/null values result in a blank output
     *
     * @param num - the numerator
     * @param div - the divisor
     * @return
     */
    public String toPercent(Object num, Object div) {
        return toPercent(num, div, true, true);
    }

    /**
     *
     * @param num
     * @param div
     * @param appendSymbol - if true the percentage symbol is appended if a
     * non-blank value
     * @param withBlanks - if true, blank numerators or divisors result in a
     * blank value. Otherwise return zero.
     * @return
     */
    public String toPercent(Object num, Object div, boolean appendSymbol, boolean withBlanks) {
        Long lNum = toLong(num, true);
        Long lDiv = toLong(div, true);
        if (lDiv == null || lDiv == 0 || lNum == null) {
            if (withBlanks) {
                return "";
            } else {
                return "0" + (appendSymbol ? "%" : "");
            }
        } else {
            long perc = lNum * 100 / lDiv;
            return perc + (appendSymbol ? "%" : "");
        }
    }


    /**
     * Removes the file extension if present
     *
     * Eg file1.swf -> file1
     *
     * file1 -> file1
     *
     * @param s
     * @return
     */
    public String stripExt(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        return FileUtils.stripExtension(s);
    }

    /**
     * True if val1 is greater then val2
     *
     * will do string conversions
     *
     * @param val1
     * @param val2
     * @return
     */
    public boolean gt(Object val1, Object val2) {
        if (val1 == null) {
            return false;
        }
        if (val2 == null) {
            return true;
        }
        Double d1 = toDouble(val1);
        Double d2 = toDouble(val2);
        return d1 > d2;
    }

    public boolean lt(Object val1, Object val2) {
        if (val1 == null) {
            return false;
        }
        if (val2 == null) {
            return true;
        }
        Double d1 = toDouble(val1);
        Double d2 = toDouble(val2);
        return d1 < d2;
    }

    public boolean eq(Object val1, Object val2) {
        if (val1 == null) {
            return (val2 == null);
        }
        if (val2 == null) {
            return false;
        }
        Double d1 = toDouble(val1);
        Double d2 = toDouble(val2);
        return d1 == d2;
    }


    /**
     * Decode percentage encoded paths. Eg a%20b -> a b
     *
     * @param s
     * @return
     */
    public String percentDecode(String s) {
        if (s == null) {
            return "";
        } else if (s.length() == 0) {
            return "";
        }
        return Utils.decodePath(s);
    }
    
    public String percentEncode(String s) {
        if( s == null ) {
            return null;
        }
        return Utils.percentEncode(s);
    }


    public Date toDate(Object oVal) {
        if (oVal == null) {
            return null;
        } else if (oVal instanceof Date) {
            return (Date) oVal;
        } else {
            if (oVal instanceof String) {
                String s = (String) oVal;
                return parseDate(s);
            } else {
                return null;
            }
        }
    }

    public java.sql.Date toSqlDate(Object oVal) {
        Date dt = toDate(oVal);
        if (dt == null) {
            return null;
        } else {
            return new java.sql.Date(dt.getTime());
        }
    }

    public java.sql.Timestamp toSqlTimestamp(Object oVal) {
        Date dt = toDate(oVal);
        if (dt == null) {
            return null;
        } else {
            return new java.sql.Timestamp(dt.getTime());
        }
    }

    public String toPlain(String html) {
        if (html == null) {
            return null;
        }
        html = replaceTag("br", html, "", "\n");
        html = replaceTag("p", html, "", "\n");
        html = replaceTag("b", html, "", "");
        html = replaceTag("i", html, "", "");
        html = replaceTag("h1", html, "", "");
        html = replaceTag("h2", html, "", "");
        html = replaceTag("h3", html, "", "");
        return html;
    }

    private String replaceTag(String tag, String html, String replaceWithOpening, String replaceWithClosing) {
        html = html.replace("<" + tag + "/>", replaceWithClosing); // self closing
        html = html.replace("<" + tag + ">", replaceWithOpening);  // opening tag
        html = html.replace("</" + tag + ">", replaceWithClosing); // closing tag
        return html;
    }


    public String getMonthName(int i) {
        switch (i) {
            case 0:
                return "January";
            case 1:
                return "February";
            case 2:
                return "March";
            case 3:
                return "April";
            case 4:
                return "May";
            case 5:
                return "June";
            case 6:
                return "July";
            case 7:
                return "August";
            case 8:
                return "September";
            case 9:
                return "October";
            case 10:
                return "November";
            case 11:
                return "December";
            default:
                return "Unknown month " + i;
        }
    }


    public String ifEqual(String ifEqual, String ifNoteEqual, Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null ? ifEqual : ifNoteEqual;
        } else {
            return o1.equals(o2) ? ifEqual : ifNoteEqual;
        }
    }

    /**
     * This just permits simple templating syntax for basic conditional values
     *
     * Eg: <li><a class="$formatter.ifTrue($item.active, 'navActive', '')"
     * href="$item.href">$item.text</a></li>
     *
     * @param b
     * @param o1
     * @param o2
     * @return
     */
    public Object ifTrue(Object bb, Object o1, Object o2) {
        Boolean b = toBool(bb);
        if (b == null) {
            b = Boolean.FALSE;
        }
        return b ? o1 : o2;
    }

    private Date parseDate(String s) {
        if (s == null || s.trim().length() == 0) {
            return null;
        }
        try {
            Date dt;
            if (s.contains(":")) {
                dt = sdf(true).parse(s);
            } else {
                dt = sdf(false).parse(s);
            }
            return dt;
        } catch (ParseException ex) {
            log.warn("couldnt parse date", ex);
            return null;
//            throw new RuntimeException(ex);
        }
    }

    public DateFormat sdf(boolean hasTime) {
        if (hasTime) {
            return sdfDateAndTime.get();
        } else {
            return sdfDateOnly.get();
        }
    }

    public BigDecimal toBigDecimal(Object o, int decimals) {
        if (o instanceof Integer) {
            Integer ii = (Integer) o;
            return new BigDecimal(ii);
        } else if (o instanceof Double) {
            Double dd = (Double) o;
            return new BigDecimal(dd).setScale(decimals, RoundingMode.HALF_UP);
        } else if (o instanceof Float) {
            Float ff = (Float) o;
            return new BigDecimal(ff);
        } else if (o instanceof String) {
            Double dd = toDouble(o);
            return toBigDecimal(dd, decimals);
        } else {
            log.warn("unhandled type: " + o.getClass());
            return null;
        }
    }

    public String checkbox(String name, Object oChecked) {          
        String s = checkbox(null, name, oChecked, "true");
        return s;
    }

    public String checkbox(String id, String name, Object oChecked) {
        return checkbox(id, name, oChecked, "true");
    }
    
    public String checkbox(String id, String name, Object oChecked, String value) {
        Boolean checked = toBool(oChecked);
        if (checked == null) {
            checked = Boolean.FALSE;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<input type='hidden' value='' name='").append(name).append(CHECKBOX_SUFFIX).append("'/>");
        sb.append("<input type=\"checkbox\"");
        sb.append(" name=\"").append(name).append("\" ");
        if (checked) {
            sb.append("checked=\"true\"");
        }
        appendValue(sb, value);
        if (id != null) {
            sb.append(" id=\"").append(id).append("\"");
        }
        sb.append(" />");        
        return sb.toString();
    }

    public String radio(String id, String name, Object oChecked, String value) {
        Boolean checked = toBool(oChecked);
        if (checked == null) {
            checked = Boolean.FALSE;
        }
        StringBuilder sb = new StringBuilder("<input type=\"radio\"");
        sb.append(" name=\"").append(name).append("\"");
        if (checked) {
            sb.append(" checked=\"true\"");
        }
        appendValue(sb, value);
        if (id != null) {
            sb.append(" id=\"").append(id).append("\"");
        }
        sb.append(" />");
        return sb.toString();
    }

    /**
     * Generate an option element
     *
     * @return
     */
    public String option(Object value, String text, Object currentValue) {
        StringBuilder sb = new StringBuilder("<option");
        appendValue(sb, value);
        if (currentValue != null && currentValue.equals(value)) {
            sb.append("selected=\"true\"");
        }
        sb.append(">");
        sb.append(text).append("</option>");
        return sb.toString();

    }

    private void appendValue(StringBuilder sb, Object value) {
        sb.append(" value=");
        sb.append("\"");
        if( value != null ) {
            sb.append(value.toString());
        }
        sb.append("\"");
    }


    public String toCsv(Iterable list) {
        StringBuilder sb = new StringBuilder();
        if (list != null) {
            for (Object o : list) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(o.toString());
            }
        }
        return sb.toString();
    }

    public String toCsv(String[] list) {
        StringBuilder sb = new StringBuilder();
        if (list != null) {
            for (Object o : list) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(o.toString());
            }
        }
        return sb.toString();
    }


    /**
     * Return a date which has the given number of days added (or subtracted if
     * negative) to the given date
     *
     * @param now
     * @param i
     * @return
     */
    public Date addDays(Date now, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return cal.getTime();
    }

}
