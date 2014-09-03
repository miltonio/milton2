package io.milton.mini.utils;

import io.milton.resource.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import io.milton.cloud.common.CurrentDateService;
import io.milton.common.FileUtils;
import io.milton.common.Utils;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.vfs.db.Profile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.milton.http.annotated.JsonWriter;
import io.milton.http.annotated.ResourceList;

/**
 * Handy functions exposes to rendering logic for formatting.
 *
 * @author brad
 */
public class Formatter {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Formatter.class);
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
    private final CurrentDateService currentDateService;

    public Formatter(CurrentDateService currentDateService) {
        this.currentDateService = currentDateService;
    }

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
            return i.intValue() == 0;
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

    public int getHour(Object o) {
        if (o == null || !(o instanceof Date)) {
            return 0;
        }
        Date dt = (Date) o;

        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public int getMinute(Object o) {
        if (o == null || !(o instanceof Date)) {
            return 0;
        }
        Date dt = (Date) o;

        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.get(Calendar.MINUTE);
    }

    public String formatDate(Object o) {
        DateTime dt = getDateTime(o);
        if (dt == null) {
            return "";
        }

        return pad2(dt.getDayOfMonth()) + "/" + pad2(dt.getMonthOfYear()) + "/" + pad(dt.getYear(), 4);
    }

    public String formatDateLong(Object o) {
        DateTime dt = getDateTime(o);
        if (dt == null) {
            return "";
        }
        return DateTimeFormat.longDateTime().print(dt);
    }

    public String formatDateISO8601(Object o) {
        DateTime dt = getDateTime(o);
        if (dt == null) {
            return "";
        } else {
            return dt.toString();
        }
    }

    /**
     * Returns a user friendly description of the age of the date. Eg "4 minutes
     * ago"
     *
     * @param o
     * @return
     */
    public String formatAge(Object o) {
        DateTime dt = getDateTime(o);
        DateTime now = new DateTime();
        Interval i;
        if (dt.isBefore(now)) {
            i = new Interval(dt, now);
        } else {
            i = new Interval(now, dt);
        }
        Duration d = i.toDuration();
        long secs = d.getStandardSeconds();
        if (secs < 10) {
            return "Just now";
        } else if (secs < 60) {
            return secs + " seconds ago";
        } else if (secs < 60 * 60) {
            return secs / 60 + " minutes ago";
        } else if (secs < 24 * 60 * 60) {
            return secs / (60 * 60) + " hours ago";
        } else {
            long days = secs / (60 * 60 * 24);
            if (days < 2) {
                return "a day ago";
            } else if (days < 30) {
                return days + " days ago";
            } else if (days < 40) {
                return "a month ago";
            } else if (days < 7 * 8) {
                return days / 7 + " weeks ago";
            } else {
                return days / 30 + " months ago";
            }
        }
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

    public org.joda.time.DateTime getDateTime(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof Resource) {
            Resource res = (Resource) o;
            return getDateTime(res.getModifiedDate());
        } else if (o instanceof String) {
            if (o.toString().length() == 0) {
                return null;
            } else {
                try {
                    Date dt = tlSdfUkShort.get().parse(o.toString());
                    return new DateTime(dt.getTime());
                } catch (ParseException ex) {
                    throw new RuntimeException("Couldnt convert to date: " + o, ex);
                }
            }
        }
        return new DateTime(o);
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

    public String format(Object o) {
        if (o == null) {
            return "";
        } else if (o instanceof Date) {
            return formatDate(o);
        } else {
            return o.toString().trim();
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
        return d1.doubleValue() > d2.doubleValue();
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
        return d1.doubleValue() < d2.doubleValue();
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
        return d1.doubleValue() == d2.doubleValue();
    }

    /**
     * Makes the given string suitable for rendering in HTML. Symbols like angle
     * brackets will be encoded so they can be displayed
     *
     * @param s
     * @return
     */
    public String htmlEncode(String s) {
        return EncodeUtils.encodeHTML(s);
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
        if (s == null) {
            return null;
        }
        return Utils.percentEncode(s);
    }

    /**
     * Returns true if the given value is between the start and finish dates, or
     * the respective values are null. Ie if start date is null and finish date
     * is given it will only check that the value is less then the finish date
     *
     * Values are converted using the joda time converters
     *
     * @param oVal
     * @param oStart
     * @param oFinish
     * @return
     */
    public boolean between(Object oVal, Object oStart, Object oFinish) {
        DateTime val = getDateTime(oVal);
        if (val == null) {
            log.warn("null date value");
            return false;
        }
        DateTime start = getDateTime(oStart);
        DateTime finish = getDateTime(oFinish);
        if (start != null) {
            if (val.isBefore(start)) {
                return false;
            }
        }
        if (finish != null) {
            if (val.isAfter(finish)) {
                return false;
            }
        }
        return true;
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

    public org.joda.time.DateTime toJodaDate(Object oVal) {
        Date dt = toDate(oVal);
        if (dt != null) {
            return new DateTime(dt.getTime());
        } else {
            return null;
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

    public Date getNow() {
        return currentDateService.getNow();
    }

    /**
     * Get the duration from the start to the finish date in seconds.
     *
     * @param start - any object which can be converted to a jodadate
     * @param finish - any object which can be converted to a jodadate
     * @return
     */
    public long durationSecs(Object start, Object finish) {
        DateTime jodaSt = toJodaDate(start);
        DateTime jodaFn = toJodaDate(finish);
        Duration d = new Duration(jodaSt, jodaFn);
        return d.getStandardSeconds();
    }

    /**
     * Get the duration from the start to the finish date in seconds.
     *
     * @param start - any object which can be converted to a jodadate
     * @param finish - any object which can be converted to a jodadate
     * @return
     */
    public long durationHours(Object start, Object finish) {
        DateTime jodaSt = toJodaDate(start);
        DateTime jodaFn = toJodaDate(finish);
        Duration d = new Duration(jodaSt, jodaFn);
        return d.getStandardSeconds() / (60 * 60);
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

    public CurrentDateService getCurrentDateService() {
        return currentDateService;
    }

    /**
     * If o1 is equal to o2, then output the ifEqual parameter, otherwise the
     * ifNoteEqual parameter
     *
     * This is a nullsafe comparison
     *
     * @param ifEqual
     * @param ifNotEqual
     * @param o1
     * @param o2
     * @return
     */
    public String ifEqual(String ifEqual, String ifNotEqual, Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null ? ifEqual : ifNotEqual;
        } else {
            if ( o2 != null && o1.getClass() == o2.getClass()) {
                return o1.equals(o2) ? ifEqual : ifNotEqual;
            } else {
                String s1 = o1.toString();
                String s2 = null;
                if (o2 != null) {
                    s2 = o2.toString();
                }
                return s1.equals(s2) ? ifEqual : ifNotEqual;
            }
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

    public ResourceList newList() {
        return new ResourceList();
    }

    public ResourceList getList() {
        return new ResourceList();
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
            return new BigDecimal(ii.intValue());
        } else if (o instanceof Double) {
            Double dd = (Double) o;
            return new BigDecimal(dd.doubleValue()).setScale(decimals, RoundingMode.HALF_UP);
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

    public String radioEq(String id, String name, String currentValue, String value) {
        boolean isSet = (currentValue != null && currentValue.equals(value));
        return radio(id, name, isSet, value);
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
    public String option(Object value, Object oText, Object currentValue) {
        String text = format(oText);
        StringBuilder sb = new StringBuilder("<option");
        appendValue(sb, value);
        sb.append(ifEqual(" selected=\"true\"", "", value, currentValue));
        sb.append(">");
        sb.append(text).append("</option>");
        return sb.toString();

    }

    /**
     * Appends the html encoded value surrounded by quotes
     *
     * @param sb
     * @param value
     */
    private void appendValue(StringBuilder sb, Object value) {
        sb.append(" value=");
        sb.append("\"");
        if (value != null) {
            sb.append(htmlEncode(value.toString()));
        }
        sb.append("\"");
    }

    /**
     * Attempts to find the port of the current request, defaults to 80
     *
     * @return
     */
    public int getPort() {
        int port = 80;
        Request req = HttpManager.request();
        if (req != null) {
            String sHost = req.getHostHeader();
            if (sHost != null) {
                String[] arr = sHost.split(":");
                if (arr.length > 1) {
                    String sPort = arr[1].trim();
                    if (sPort.length() > 0) {
                        port = Integer.parseInt(sPort);
                    }
                }
            }
        }
        return port;
    }

    /**
     * Returns empty string if the current request is on port 80, otherwise
     * returns the port number prefixed with a colon, eg :8080
     *
     * @return
     */
    public String getPortString() {
        int p = getPort();
        if (p == 80) {
            return "";
        } else {
            return ":" + p;
        }
    }

    public String profilePicHref(Profile p) {
        if (p.getPhotoHash() == null) {
            return "/templates/apps/user/profile.png";
        } else {
            return "/_hashes/files/" + p.getPhotoHash();
        }
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

    public String toJson(Object val) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        JsonWriter jsonWriter = new JsonWriter();
        jsonWriter.write(val, bout);
        return bout.toString("UTF-8");
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


    public boolean isNotNull(Object o) {
        return o != null && !o.equals("");
    }

    public boolean isNull(Object o) {
        return o == null || o.equals("");
    }


}
