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
package io.milton.http.webdav;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 *
 * Represents the destination of a MOVE or COPY method
 *
 * The target url is parsed into the 3 components, host, parent url and name to
 * make it easier to differentiate between the name and folder of the
 * destination
 */
public class Dest {

    private static final Logger log = LoggerFactory.getLogger(Dest.class);
    public final String host;
    /**
     * path of the parent folder
     */
    public final String url;
    /**
     * the name portion of the destination
     */
    public final String name;

    public Dest(String sourceHost, String sDest) {
        log.debug("sDest: " + sDest);
        String sUrl;
        if (sDest.endsWith("/")) {
            sDest = sDest.substring(0, sDest.length() - 1);
        }
        if (sDest.contains("http://")) {
            String s = sDest.replace("http://", "");
            int pos = s.indexOf(":");
            if (pos > 0) {
                host = s.substring(0, pos);
                pos = s.indexOf("/");
            } else {
                pos = s.indexOf("/");
                host = s.substring(0, pos);
            }
            sUrl = s.substring(pos);
        } else {
            host = sourceHost;
            sUrl = sDest;
        }
        int pos = sUrl.lastIndexOf("/");
        if (pos <= 0) {
            url = "/";
        } else {
            url = sUrl.substring(0, pos);
        }
        name = sUrl.substring(pos + 1);
    }

    @Override
    public String toString() {
        return host + ":" + url + "/" + name;
    }
}