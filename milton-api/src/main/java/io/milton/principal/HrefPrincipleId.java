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

package io.milton.principal;

import javax.xml.namespace.QName;

/**
 *
 * @author brad
 */
public class HrefPrincipleId implements Principal.PrincipleId {

    private final QName type;
    private final String url;

    public HrefPrincipleId(String url) {
        this.url = url;
        this.type = new QName("DAV:", "href");
    }

    @Override
    public QName getIdType() {
        return type;
    }

    @Override
    public String getValue() {
        return url;
    }

    @Override
    public String toString() {
        return url;
    }
}
