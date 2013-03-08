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
package io.milton.dns.resource;

import java.net.InetAddress;

/**
 * @   IN  SOA     nameserver.place.dom.  postmaster.place.dom. (
                               1            ; serial number
                               3600         ; refresh   [1h]
                               600          ; retry     [10m]
                               86400        ; expire    [1d]
                               3600 )       ; min TTL   [1h]
 * 
 *
 * @author brad
 */
public interface SOADomainResourceRecord extends DomainResourceRecord {
    /**
     * The host name of the server that generated this zone file
     * 
     * @return 
     */
    String getHost();

    /**
     * Email address of the administrator.
     * 
     * The e-mail address of the person responsible for administering the domain's zone file.
     * 
     * @return 
     */
    String getAdminEmail();

    /**
     * Counter which should be incremented when the zone file changes
     * 
     * The revision number of this zone file. Increment this number each time the zone file is changed. It is important to increment this value each time a change is made, so that the changes will be distributed to any secondary DNS servers. 
     * 
     * @return 
     */
    long getZoneSerialNumber();
    
    /** The time, in seconds, a secondary DNS server waits before querying the primary DNS server's SOA record to check for changes. When the refresh time expires, the secondary DNS server requests a copy of the current SOA record from the primary. The primary DNS server complies with this request. The secondary DNS server compares the serial number of the primary DNS server's current SOA record and the serial number in it's own SOA record. If they are different, the secondary DNS server will request a zone transfer from the primary DNS server. The default value is 3,600. 
     * 
     * Eg 7200
     * @return 
     */
    long getRefresh();
    
    /**
     * The time, in seconds, a secondary server waits before retrying a failed zone transfer. Normally, the retry time is less than the refresh time. Eg 600. 
     * 
     * @return 
     */
    long getRetry();
    
    /**
     * The time, in seconds, that a secondary server will keep trying to complete a zone transfer. If this time expires prior to a successful zone transfer, the secondary server will expire its zone file. This means the secondary will stop answering queries, as it considers its data too old to be reliable. The default value is 86,400. 
     * 
     * @return 
     */
    long getExpire();
    
    /**
     * The minimum time-to-live value applies to all resource records in the zone file. This value is supplied in query responses to inform other servers how long they should keep the data in cache. The default value is 3,600. 
     * 
     * @return 
     */
    long getMinimum();
}
