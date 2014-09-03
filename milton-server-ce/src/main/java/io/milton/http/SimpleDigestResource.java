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
