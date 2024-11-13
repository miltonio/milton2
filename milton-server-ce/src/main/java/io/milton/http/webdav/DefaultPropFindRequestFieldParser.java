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

import io.milton.common.StreamUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Simple implmentation which just parses the request body. If no xml is present
 * it will return an empty set.
 * <p>
 * Note this generally shouldnt be used directly, but should be wrapped by
 * MSPropFindRequestFieldParser to support windows clients.
 *
 * @author brad
 */
public class DefaultPropFindRequestFieldParser implements PropFindRequestFieldParser {

    private static final Logger log = LoggerFactory.getLogger(DefaultPropFindRequestFieldParser.class);

    public DefaultPropFindRequestFieldParser() {
    }

    @Override
    public PropertiesRequest getRequestedFields(InputStream in) {
        final Set<QName> set = new LinkedHashSet<>();
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            StreamUtils.readTo(in, bout, false, true);
            byte[] arr = bout.toByteArray();
            if (arr.length > 1) {
                ByteArrayInputStream bin = new ByteArrayInputStream(arr);
                XMLReader reader = XMLReaderFactory.createXMLReader();
                reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                // https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
                reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
                PropFindSaxHandler handler = new PropFindSaxHandler();
                reader.setContentHandler(handler);
                try {
                    reader.parse(new InputSource(bin));
                    if (handler.isAllProp() || handler.isPropname()) {
                        return new PropertiesRequest(handler.isAllProp(), handler.isPropname());
                    } else {
                        set.addAll(handler.getAttributes().keySet());
                    }
                } catch (IOException | SAXException e) {
                    log.warn("exception parsing request body", e);
                    // ignore
                }
            }
        } catch (Exception ex) {
            // There's a report of an exception being thrown here by IT Hit Webdav client
            // Perhaps we can just log the error and return an empty set. Usually this
            // class is wrapped by the MsPropFindRequestFieldParser which will use a default
            // set of properties if this returns an empty set
            log.warn("Exception parsing PROPFIND request fields. Returning empty property set", ex);
            //throw new RuntimeException( ex );
        }
        return PropertiesRequest.toProperties(set);
    }
}
