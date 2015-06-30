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
package io.milton.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as one which creates calendars. A calendar is just a collection
 * which contains events.
 * 
 * <p>Your method should have the following arguments:
 * <ul>
 * <li>The target object which must be the source for the calendars home</li>
 * <li>The name of the new calendar</li>
 * <li>The initial fields to set: Map{@literal<}QName, String{@literal>} fieldsToSet</li>
 * </ul>
 * 
 * <p>The make calendar method should apply the given fields to the calendar and persist
 * 
 * <p>Return the source object for the new calendar
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MakeCalendar {
    
}
