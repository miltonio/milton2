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
