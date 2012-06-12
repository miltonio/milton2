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

package io.milton.http.quota;

import io.milton.http.QuotaResource;
import io.milton.resource.Resource;

/**
 * Default implementation which just reads the quota properties from
 * QuotaResource, if the given resource implements it. Otherwise
 * returns null;
 *
 * @author brad
 */
public class DefaultQuotaDataAccessor implements QuotaDataAccessor {

    public Long getQuotaAvailable( Resource res ) {
        if( res instanceof QuotaResource ) {
            QuotaResource quotaRes = (QuotaResource) res;
            Long l = quotaRes.getQuotaAvailable();
            return l;
        } else {
            return null;
        }

    }

    public Long getQuotaUsed( Resource res ) {
        if( res instanceof QuotaResource ) {
            QuotaResource quotaRes = (QuotaResource) res;
            Long l = quotaRes.getQuotaUsed();
            return l;
        } else {
            return null;
        }
    }
}
