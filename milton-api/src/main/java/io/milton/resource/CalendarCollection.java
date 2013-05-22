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

import io.milton.resource.CollectionResource;

/**
 * 
 *
 * @author brad
 */
public interface CalendarCollection extends CollectionResource {
    /**
     *  For each calendar or scheduling Inbox or Outbox collection on the
     *   server, a new CS:getctag WebDAV property is present.
     *
     *   The property value is an "opaque" token whose value is guaranteed to
     *   be unique over the lifetime of any calendar or scheduling Inbox or
     *   Outbox collection at a specific URI.
     *
     *   Whenever a calendar resource is added to, modified or deleted from
     *   the calendar collection, the value of the CS:getctag property MUST
     *   change.  Typically this change will occur when the DAV:getetag
     *   property on a child resource changes due to some protocol action.  It
     *   could be the result of a change to the body or properties of the
     *   resource.

     *
     * http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-ctag.txt
     *
     * @return
     */
    String getCTag();
    
}
