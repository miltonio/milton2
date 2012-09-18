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

package io.milton.http.json;

import io.milton.http.Auth;
import io.milton.http.HttpManager;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.GetableResource;
import io.milton.resource.PropFindableResource;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropFindJsonResource extends JsonResource implements GetableResource {

    private static final Logger log = LoggerFactory.getLogger( PropFindJsonResource.class );

    private final PropFindableResource wrappedResource;
    private final JsonPropFindHandler jsonPropFindHandler;
    private final String encodedUrl;

    public PropFindJsonResource(PropFindableResource wrappedResource, JsonPropFindHandler jsonPropFindHandler, String encodedUrl, Long maxAgeSecs) {
        super(wrappedResource, Request.Method.PROPFIND.code, maxAgeSecs);
        this.wrappedResource = wrappedResource;
        this.encodedUrl = encodedUrl;
        this.jsonPropFindHandler = jsonPropFindHandler;
    }

	@Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
        //jsonPropFindHandler.sendContent( wrappedResource, encodedUrl, out, range, params, contentType );
        jsonPropFindHandler.sendContent(wrappedResource, encodedUrl, out, range, params, contentType);
    }

    @Override
    public Method applicableMethod() {
        return Method.PROPFIND;
    }

    /**
     * Overridden to allow clients to specifiy the max age as a request parameter
     *
     * This is to allow efficient browser caching of results in cases that need it,
     * while also permitting non-cached access
     *
     * @param auth
     * @return
     */
    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        Request req = HttpManager.request();
        if (req != null) {
            String sMaxAge = req.getParams().get("maxAgeSecs");
            if (sMaxAge != null && sMaxAge.length() > 0) {
                try {
                    log.trace("using max age from parameter");
                    Long maxAge = Long.parseLong(sMaxAge);
                    return maxAge;
                } catch (NumberFormatException e) {
                    log.debug("Couldnt parse max age parameter: " + sMaxAge);
                }
            }
        }
        return super.getMaxAgeSeconds(auth);
    }

	
}
