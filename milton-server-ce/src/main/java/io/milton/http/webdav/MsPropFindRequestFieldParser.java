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

import javax.xml.namespace.QName;
import java.io.InputStream;

/**
 * Decorator for PropFindRequestFieldParser's.
 * <p>
 * Calls getRequestedFields on the wrapped object. If no fields were requested
 * this class adds the default ones expected
 * by windows clients. This is because windows clients generally do not
 * specify a PROPFIND body and expect the server to return these fields.
 * <p>
 * Note that failing to return exactly the fields expected in the exact order
 * can break webdav on windows.
 *
 * @author brad
 */
public class MsPropFindRequestFieldParser implements PropFindRequestFieldParser {

    private final PropFindRequestFieldParser wrapped;

    public MsPropFindRequestFieldParser(PropFindRequestFieldParser wrapped) {
        this.wrapped = wrapped;
    }


    @Override
    public PropertiesRequest getRequestedFields(InputStream in) {
        PropertiesRequest result = wrapped.getRequestedFields(in);
        if (result.isAllProp() || result.isPropname()) {
            return result;
        }
        if (result.getNames().isEmpty()) {
            add(result, "creationdate");
            add(result, "getlastmodified");
            add(result, "displayname");
            add(result, "resourcetype");
            add(result, "getcontenttype");
            add(result, "getcontentlength");
            add(result, "getetag");
        }
        return result;
    }

    private void add(PropertiesRequest result, String name) {
        QName qname = new QName(WebDavProtocol.NS_DAV.getName(), name);
        result.add(qname);
    }

}
