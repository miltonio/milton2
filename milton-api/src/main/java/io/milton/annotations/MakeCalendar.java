/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.milton.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as one which creates calendars. A calendar is just a collection
 * which contains events.
 * 
 * Your method should have the following arguments:
 * <ul>
 * <li>The target object which must be the source for the calendars home</li>
 * <li>The name of the new calendar</li>
 * <li>The initial fields to set: Map<QName, String> fieldsToSet</li>
 * </ul>
 * 
 * The make calendar method should apply the given fields to the calendar and persist
 * 
 * Return the source object for the new calendar
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MakeCalendar {
    
}
