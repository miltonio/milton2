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

import io.milton.resource.Resource;

/**
 * Resource interface to support quota's
 *
 * This must be implemented by Collection (ie Folder) resources if you want to
 * support quotas
 *
 * Resources which implement this can have their quota availability reported to
 * client applications via the RFC4331 HTTP extensions
 *
 * See: http://www.faqs.org/rfcs/rfc4331.html
 *
 * Note that these properties may or may not be used in quota checking on PUT.
 * The PutHandler delegates to a StorageChecker which might be configured to
 * use a StorageChecker which uses this information.
 *
 * @author brad
 */
public interface QuotaResource extends Resource {
    /**
     * From the spec:
     *
     * "The DAV:quota-used-bytes value is the value in octets representing
   the amount of space used by this resource and possibly a number of
   other similar resources, where the set of "similar" meets at least
   the criterion that allocating space to any resource in the set will
   count against the DAV:quota-available-bytes.  It MUST include the
   total count including usage derived from sub-resources if
   appropriate.  It SHOULD include metadata storage size if metadata
   storage is counted against the DAV:quota-available-bytes.

   Note that there may be a number of distinct but overlapping sets of
   resources for which a DAV:quota-used-bytes is maintained (e.g., "all
   files with a given owner", "all files with a given group owner",
   etc.).  The server is at liberty to choose any of those sets but
   SHOULD do so in a repeatable way.  The rule may be configured per
   repository.


Read more: http://www.faqs.org/rfcs/rfc4331.html#ixzz0bnfikrSE
"
     *
     * @return - the number of bytes used in this quota allocation, or null
     * to indicate this information is not available
     */
    Long getQuotaUsed();


    /**
     * From the spec:
     *
     The DAV:quota-available-bytes property value is the value in octets
   representing the amount of additional disk space beyond the current
   allocation that can be allocated to this resource before further
   allocations will be refused.  It is understood that this space may be
   consumed by allocations to other resources.

   Support for this property is REQUIRED on collections, and OPTIONAL on
   other resources.  A server SHOULD implement this property for each
   resource that has the DAV:quota-used-bytes property.

   Clients SHOULD expect that as the DAV:quota-available-bytes on a
   resource approaches 0, further allocations to that resource may be
   refused.  A value of 0 indicates that users will probably not be able
   to perform operations that write additional information (e.g., a PUT
   inside a collection), but may be able to replace through overwrite an
   existing resource of equal size.


Read more: http://www.faqs.org/rfcs/rfc4331.html#ixzz0bnfxRSVV
*
     *
     * @return - the number of bytes used in this quota allocation, or null
     * to indicate this information is not available
     */
    Long getQuotaAvailable();
}
