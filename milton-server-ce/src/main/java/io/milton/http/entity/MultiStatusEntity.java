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

package io.milton.http.entity;

import io.milton.http.HrefStatus;
import io.milton.http.Response;
import io.milton.http.XmlWriter;
import io.milton.http.webdav.WebDavProtocol;

import java.io.OutputStream;
import java.util.List;

public class MultiStatusEntity implements Response.Entity {

    private List<HrefStatus> statii;

    public MultiStatusEntity(List<HrefStatus> statii) {
        this.statii = statii;
    }

    public List<HrefStatus> getStatii() {
        return statii;
    }

    @Override
    public void write(Response response, OutputStream outputStream) throws Exception {

        XmlWriter writer = new XmlWriter( response.getOutputStream() );
        writer.writeXMLHeader();
        writer.open( "multistatus xmlns:D" + "=\"" + WebDavProtocol.NS_DAV + ":\"" ); // only single namespace for this method
        writer.newLine();
        for( HrefStatus status : getStatii()) {
            XmlWriter.Element elResponse = writer.begin( "response" ).open();
            writer.writeProperty( "", "href", status.href );
            writer.writeProperty( "", "status", status.status.code + "" );
            elResponse.close();
        }
        writer.close( "multistatus" );
        writer.flush();
    }
}
