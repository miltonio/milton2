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

package io.milton.resource;

import io.milton.http.values.SupportedCalendarComponentList;

/**
 *
 * @author alex
 */
public interface CalendarResource extends CalendarCollection, PropFindableResource {

    public enum ComponentType {
        VEVENT,
        VTODO,
        VTIMEZONE,
        VFREEBUSY
    }
    
    String getCalendarDescription();

    String getColor();
    
    void setColor(String s);	

    /**
     * If there is a restriction as to what types of components may be created within
     * this calendar collection, then return the allowed components. If there are
     * no restrictions return null.
     * 
     * @return 
     */
    SupportedCalendarComponentList getSupportedComponentSet();
    
}
