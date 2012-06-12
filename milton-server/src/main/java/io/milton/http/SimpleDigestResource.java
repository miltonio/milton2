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

import io.milton.resource.DigestResource;
import io.milton.http.http11.auth.DigestResponse;
import java.util.Date;

/**
 *
 * @author brad
 */
public class SimpleDigestResource extends SimpleResource implements DigestResource{

    private final DigestResource digestResource;

    public SimpleDigestResource( String name, Date modDate, byte[] content, String contentType, String uniqueId, String realm) {
        super(name, modDate, content, contentType, uniqueId, realm );
        this.digestResource = null;
    }

    public SimpleDigestResource( String name, Date modDate, byte[] content, String contentType, String uniqueId, DigestResource secureResource ) {
        super(name, modDate, content, contentType, uniqueId, secureResource );
        this.digestResource = secureResource;
    }

    public Object authenticate( DigestResponse digestRequest ) {
        return digestResource.authenticate( digestRequest );
    }

    public boolean isDigestAllowed() {
        return true;
    }


}
