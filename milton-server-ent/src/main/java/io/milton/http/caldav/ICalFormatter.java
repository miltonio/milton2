/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */
package io.milton.http.caldav;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * A helper class to parse and format the iCalendar specification. A very simple
 * implement to support DefaultCalendarSearchService. You will probably want
 * to replace both in a production implementation
 *
 * To use this, first implement the appropriate interface (E.g. EventResource
 * for VEVENT) and then use this class to implement getICalData
 *
 * Eg
 *
 * public String getICalData() { ICalFormatter formatter = new ICalFormatter();
 * return formatter.formatEvent( this ); }
 *
 * @author brad
 */
public class ICalFormatter {

    public static final String MAILTO = "mailto:";
    // Changed T to space. removed trailing Z
    private static final String PATTERN_ICAL = "yyyyMMdd HHmmss";

    public ICalFormatter() {
    }

    public void parseEvent(EventResource r, String data) throws IOException, ParserException {
        CalendarBuilder builder = new CalendarBuilder();
        net.fortuna.ical4j.model.Calendar calendar = builder.build(new ByteArrayInputStream(data.getBytes("UTF-8")));
        VEvent ev = event(calendar);
        String summary = null;
        if (ev.getSummary() != null) {
            summary = ev.getSummary().getValue();
        }
        r.setSummary(summary);
        r.setStart(ev.getStartDate().getDate());
        Date endDate = null;
        if (ev.getEndDate() != null) {
            endDate = ev.getEndDate().getDate();
        }
        r.setEnd(endDate);
    }

    /**
     * Return the attendee mailtos for some sort of ical request
     * @param data
     * @return 
     */
    public List<String> parseAttendees(String data) {
        String[] lines = toLines(data);
        List<String> attendees = new ArrayList<String>();
        for (String line : lines) {
            if (line != null && line.contains(":")) {
                int pos = line.indexOf(":");
                String key = line.substring(0, pos);
                String val = line.substring(pos + 1);
                if (key.contains("ATTENDEE")) {
                    String s = getMailTo(val);
                    attendees.add(s);
                }
            }
        }
        return attendees;
    }
    
    public FreeBusyRequest parseFreeBusyRequest(String data) {
        FreeBusyRequest r = new FreeBusyRequest();
        String[] lines = toLines(data);
        List<String> attendees = new ArrayList<String>();
        r.setAttendeeMailtos(attendees);
        for (String line : lines) {
            if (line != null && line.contains(":")) {
                int pos = line.indexOf(":");
                String key = line.substring(0, pos);
                String val = line.substring(pos + 1);
                r.getLines().put(key, line);
                if (key.equals("DTSTART")) {
                    Date dt = parseDate(val);
                    r.setStart(dt);
                } else if (key.equals("DTEND")) {
                    Date dt = parseDate(val);
                    r.setFinish(dt);
                } else if (key.equals("ORGANIZER")) {
                    String s = getMailTo(val);
                    System.out.println("org: " + s);
                    r.setOrganiserMailto(s);
                } else if (key.contains("ATTENDEE")) {
                    System.out.println("found attendee");
                    String s = getMailTo(val);
                    System.out.println("attendee: " + s);
                    attendees.add(s);
                    r.getAttendeeLines().put(s, line);
                }
            }
        }
        return r;
    }

    public String formatEvent(EventResource r) {
        return "BEGIN:VCALENDAR\n"
                + "VERSION:2.0\n"
                + "PRODID:-//MiltonCalDAV//EN\n"
                + "BEGIN:VEVENT\n"
                + "UID:" + r.getUniqueId() + "\n"
                + "DTSTAMP:19970714T170000Z\n"
                + "SUMMARY:" + r.getSummary() + "\n"
                + "DTSTART:" + formatDate(r.getStart()) + "\n"
                + "DTEND:" + formatDate(r.getEnd()) + "\n"
                + "END:VEVENT\n"
                + "END:VCALENDAR";
    }

    public Date parseDate(String s) {
        if (s == null || s.length() == 0) {
            return null;
        }
        s = s.replace("T", " ");
        s = s.replace("Z", "");
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_ICAL);
        try {
            return sdf.parse(s);
        } catch (ParseException ex) {
            throw new RuntimeException(s, ex);
        }
    }

    public String formatDate(Date d) {
        if (d == null) {
            return "";
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        StringBuilder sb = new StringBuilder();
        sb.append(cal.get(Calendar.YEAR)).append("");
        sb.append(pad2(cal.get(Calendar.MONTH) + 1));
        sb.append(pad2(cal.get(Calendar.DAY_OF_MONTH)));
        sb.append('T');
        sb.append(pad2(cal.get(Calendar.HOUR_OF_DAY)));
        sb.append(pad2(cal.get(Calendar.MINUTE)));
        sb.append(pad2(cal.get(Calendar.SECOND)));
        sb.append('Z');
        String s = sb.toString();
        return s;
    }

    private static String pad2(int i) {
        if (i < 10) {
            return "0" + i;
        } else {
            return i + "";
        }
    }

    private String getMailTo(String s) {
        // CN="Wilfredo Sanchez Vega":mailto:wilfredo@example.com
        int pos = s.lastIndexOf(MAILTO);
        if (pos >= 0) {
            String m = s.substring(pos + MAILTO.length(), s.length());
            return m;
        } else {
            return null;
        }

    }

    private String[] toLines(String data) {
        List<String> lines = new ArrayList<String>();
        for (String s : data.split("\n")) {
            if (s.startsWith(" ")) {
                s = s.trim();
                String line = lines.get(lines.size() - 1) + s;
                lines.remove(lines.size() - 1);
                lines.add(line);
            } else {
                s = s.trim();
                lines.add(s);
            }
        }
        String[] arr = new String[lines.size()];
        return lines.toArray(arr);
    }

    private VEvent event(net.fortuna.ical4j.model.Calendar cal) {
        return (VEvent) cal.getComponent("VEVENT");
    }

    public class FreeBusyRequest {

        private Date start;
        private Date finish;
        private String organiserMailto;
        private List<String> attendeeMailtos;
        private Map<String, String> attendeeLines = new HashMap<String, String>();
        private Map<String, String> lines = new HashMap<String, String>();

        public Date getStart() {
            return start;
        }

        public void setStart(Date start) {
            this.start = start;
        }

        public Date getFinish() {
            return finish;
        }

        public void setFinish(Date finish) {
            this.finish = finish;
        }

        public String getOrganiserMailto() {
            return organiserMailto;
        }

        public void setOrganiserMailto(String organiserMailto) {
            this.organiserMailto = organiserMailto;
        }

        public List<String> getAttendeeMailtos() {
            return attendeeMailtos;
        }

        public void setAttendeeMailtos(List<String> attendeeMailtos) {
            this.attendeeMailtos = attendeeMailtos;
        }

        public Map<String, String> getLines() {
            return lines;
        }

        public Map<String, String> getAttendeeLines() {
            return attendeeLines;
        }
    }
}
