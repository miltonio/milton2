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

package io.milton.http;

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
