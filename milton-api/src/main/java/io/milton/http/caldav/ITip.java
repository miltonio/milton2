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

package io.milton.http.caldav;

/**
 *
 * @author brad
 */
public class ITip {

    public enum StatusResponse {

        RS_SUCCESS_20("2.0", "Success.", "None."),
        RS_SUCCESS_21("2.1", "Success but fallback taken on one or more property values.", "Property name and value MAY be specified."),
        RS_SUCCESS_22("2.2", "Success, invalid property ignored.", "Property name MAY be specified."),
        RS_SUCCESS_23("2.3", "Success, invalid property parameter ignored.", "Property parameter name and value MAY be specified."),
        RS_SUCCESS_24("2.4", "Success, unknown non-standard property ignored.", "Non-standard property name MAY be specified."),
        RS_SUCCESS_25("2.5", "Success, unknown non standard property value ignored.", "Property and non-standard value MAY be specified."),
        RS_SUCCESS_26("2.6", "Success, invalid calendar component ignored.", "Calendar component sentinel (e.g., BEGIN:ALARM) MAY be specified."),
        RS_SUCCESS_27("2.7", "Success, request forwarded to Calendar User.", "Original and forwarded caluser addresses MAY be specified."),
        RS_SUCCESS_28("2.8", "Success, repeating event ignored. Scheduled as a single component.", "RRULE or RDATE property name and value MAY be specified."),
        RS_SUCCESS_29("2.9", "Success, truncated end date time to date boundary.", "DTEND property value MAY be specified."),
        RS_SUCCESS_210("2.10", "Success, repeating VTODO ignored. Scheduled as a single VTODO.", "RRULE or RDATE property name and value MAY be specified."),
        RS_SUCCESS_211("2.11", "Success, unbounded RRULE clipped at some finite number of instances", "RRULE property name and value MAY be specified. Number of instances MAY also be specified."),
        //Invalid Data Responses
        //Status Code 	Longer Return Status Description 	Offending Data"),
        RS_INVALID_30("3.0", "Invalid property name.", "Property name MAY be specified."),
        RS_INVALID_31("3.1", "Invalid property value.", "Property name and value MAY be specified."),
        RS_INVALID_32("3.2", "Invalid property parameter.", "Property parameter name and value MAY be specified."),
        RS_INVALID_33("3.3", "Invalid property parameter value.", "Property parameter name and value MAY be specified."),
        RS_INVALID_34("3.4", "Invalid calendar componentsequence.", "Calendar component sentinel MAY be specified (e.g., BEGIN:VTIMEZONE)."),
        RS_INVALID_35("3.5", "Invalid date or time.", "Date/time value(s) MAY be specified."),
        RS_INVALID_36("3.6", "Invalid rule.", "Rule value MAY be specified."),
        RS_INVALID_37("3.7", "Invalid Calendar User.", "Attendee property value MAY be specified."),
        RS_INVALID_38("3.8", "No authority.", "METHOD and Attendee property values MAY be specified."),
        RS_INVALID_39("3.9", "Unsupported version.", "VERSION property name and value MAY be specified."),
        RS_INVALID_310("3.10", "Request entity too large.", "None."),
        RS_INVALID_311("3.11", "Required component or property missing.", "Component or property name MAY be specified."),
        RS_INVALID_312("3.12", "Unknown component or property found ", "Component or property name MAY be specified"),
        RS_INVALID_313("3.13", "Unsupported component or property found", "Component or property name MAY be specified"),
        RS_INVALID_314("3.14", "Unsupported capability", "Method or action MAY be specified"),
        //Conflict Response
        //Status Code 	Longer Return Status Description 	Offending Data
        RS_CONFLICT_400("4.0", "Event conflict. Date/time is busy.", "DTSTART and DTEND property name and values MAY be specified."),
        //ERROR Responses
        //Status Code 	Longer Return Status Description 	Offending Data
        RS_ERROR_50("5.0", "Request MAY supported. (WTF?!)", "Method property value MAY be specified."),
        RS_ERROR_51("5.1", "Service unavailable.", "ATTENDEE property value MAY be specified."),
        RS_ERROR_52("5.2", "Invalid calendar service.", "ATTENDEE property value MAY be specified."),
        RS_ERROR_53("5.3", "No scheduling support for user.", "ATTENDEE property value MAY be specified.         ");
        public String code;
        public String description;
        public String longerDesc;

        StatusResponse(String code, String description, String longerDesc) {
            this.code = code;
            this.description = description;
            this.longerDesc = longerDesc;
        }
    }
}
