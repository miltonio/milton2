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
package io.milton.http.webdav2;

import io.milton.http.webdav.ResourceTypeHelper;
import io.milton.http.webdav.WebDavResourceTypeHelper;
import io.milton.resource.LockableResource;
import io.milton.resource.Resource;
import java.util.List;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Advertises support for DAV level 2 for resources which support locking
 *
 * @author brad
 */
public class WebDavLevel2ResourceTypeHelper implements ResourceTypeHelper {

    private static final Logger log = LoggerFactory.getLogger(WebDavResourceTypeHelper.class);

    private final ResourceTypeHelper wrapped;

    public WebDavLevel2ResourceTypeHelper(ResourceTypeHelper wrapped) {
        this.wrapped = wrapped;
    }
    
    
    
    @Override
    public List<QName> getResourceTypes(Resource r) {
        return wrapped.getResourceTypes(r);
    }

    //Need to create a ArrayList as Arrays.asList returns a fixed length list which
    //cannot be extended.
    @Override
    public List<String> getSupportedLevels(Resource r) {
        List<String> list = wrapped.getSupportedLevels(r);
        if (r instanceof LockableResource) {
            list.add("2");
        }
        return list;
    }
}
