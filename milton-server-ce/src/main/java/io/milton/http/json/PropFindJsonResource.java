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
