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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.milton.common.LogUtils;
import io.milton.http.Range;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.values.ValueAndType;
import io.milton.http.webdav.PropFindPropertyBuilder;
import io.milton.http.webdav.PropFindResponse;
import io.milton.http.webdav.PropertiesRequest;
import io.milton.http.webdav.PropertiesRequest.Property;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.resource.CollectionResource;
import io.milton.resource.PropFindableResource;
import io.milton.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author brad
 */
public class JsonPropFindHandler {

    private static final Logger log = LoggerFactory.getLogger(JsonPropFindHandler.class);
    private final PropFindPropertyBuilder propertyBuilder;
    private final Helper helper;

    public JsonPropFindHandler(PropFindPropertyBuilder propertyBuilder) {
        this.propertyBuilder = propertyBuilder;
        helper = new Helper();
    }

    public void sendContent(PropFindableResource wrappedResource, String encodedUrl, OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
        log.debug("sendContent: {}", encodedUrl);
        ObjectMapper mapper = ObjectMapperFactory.mapper();

        Object json;
        String[] arr;
        if (propertyBuilder == null) {
            if (wrappedResource instanceof CollectionResource) {
                List<? extends Resource> children = Optional.ofNullable(((CollectionResource) wrappedResource).getChildren()).orElse(List.of());
                json = toSimpleList(children);
            } else {
                json = toSimple(wrappedResource);
            }
        } else {
            // use propfind handler
            String sFields = params.get("fields");
            Set<QName> fields = new HashSet<>();
            Map<QName, String> aliases = new HashMap<>();
            if (sFields != null && !sFields.isEmpty()) {
                arr = sFields.split(",");
                for (String s : arr) {
                    parseField(s, fields, aliases);
                }
            }

            String sDepth = params.get("depth");
            int depth = 1;
            if (sDepth != null && !sDepth.trim().isEmpty()) {
                depth = Integer.parseInt(sDepth);
            }


            String href = encodedUrl.replace("/_DAV/PROPFIND", "");
            PropertiesRequest parseResult = new PropertiesRequest(toProperties(fields));
            LogUtils.debug(log, "prop builder: ", propertyBuilder.getClass(), "href", href);
            List<PropFindResponse> props;
            try {
                props = propertyBuilder.buildProperties(wrappedResource, depth, parseResult, href);
            } catch (URISyntaxException ex) {
                throw new RuntimeException("Requested url is not properly encoded: " + href, ex);
            }

            String where = params.get("where");
            filterResults(props, where);

            json = helper.toMap(props, aliases);
        }
        mapper.writeValue(out, json);
    }

    private Set<Property> toProperties(Set<QName> set) {
        Set<Property> props = new HashSet<>();
        for (QName n : set) {
            props.add(new Property(n, null));
        }
        return props;
    }

    /**
     * Parse the given field and populate the given maps
     * <p>
     * A field may be in the following forms
     * - foo
     * - DAV:foo
     * - DAV:foo>bar
     * - foo>bar
     * <p>
     * The first is just a property named foo.
     * The second is a property called foo in the namespace DAV
     * The third includes an alias so the property is returned with the name "bar" in the json object
     * The final shows that an alias can be used without a namespace
     *
     * @param field
     * @param fields
     */
    void parseField(String field, Set<QName> fields, Map<QName, String> aliases) {
        String alias = null;
        if (field.contains(">")) {
            int pos = field.indexOf(">");
            alias = field.substring(pos + 1);
            field = field.substring(0, pos);
        }
        QName qn = parseQName(field);
        //log.debug("field: " + qn);
        fields.add(qn);
        if (alias != null) {
            aliases.put(qn, alias);
        }
    }

    private QName parseQName(String field) {
        if (field.contains(":")) {
            // name is of form uri:local  E.g. MyDav:authorName
            String[] parts = field.split(":");
            String nsUri = parts[0];
            String localName = parts[1];
            return new QName(nsUri, localName);
        } else {
            // name is simple form E.g. displayname, default nsUri to DAV
            return new QName(WebDavProtocol.NS_DAV.getName(), field);
        }

    }

    private List<SimpleResource> toSimpleList(List<? extends Resource> list) {
        List<SimpleResource> simpleList = new ArrayList<>(list.size());
        for (Resource r : list) {
            simpleList.add(toSimple(r));
        }
        return simpleList;
    }

    private SimpleResource toSimple(Resource r) {
        return new SimpleResource(r);
    }

    /**
     * If the where argument is given, removes results where it does not
     * evaluate to true
     * <p>
     * If the given where argument starts with ! the condition is negated
     *
     * @param results
     * @param where
     */
    private void filterResults(List<PropFindResponse> results, String where) {
        if (where != null && !where.isEmpty()) {
            boolean negate = where.startsWith("!");
            if (negate) {
                where = where.substring(1);
            }
            QName qnWhere = parseQName(where);
            Iterator<PropFindResponse> it = results.iterator();
            while (it.hasNext()) {
                PropFindResponse result = it.next();
                boolean isTrue = eval(qnWhere, result);
                // eg !iscollection for a folder -> false == false = true, so remove
                // eg !iscollection for a file -> true == false = false, dont remove
                // eg iscollection for a folder -> false == true = false, so dont remove
                if (isTrue == negate) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Find a boolean value from the given propery name on the propfind
     * result.
     * <p>
     * Absense of the property, or a value which
     * cannot be interpreted as boolean, implies false.
     *
     * @param qnWhere
     * @param result
     * @return
     */
    private boolean eval(QName qnWhere, PropFindResponse result) {
        ValueAndType prop = result.getKnownProperties().get(qnWhere);
        if (prop != null) {
            Object val = prop.getValue();
            if (val instanceof Boolean) {
                return (Boolean) val;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    public static class SimpleResource {

        private final Resource r;

        public SimpleResource(Resource r) {
            this.r = r;
        }

        public String getName() {
            return r.getName();
        }

        public Date getModifiedDate() {
            return r.getModifiedDate();
        }
    }

    static class Helper {

        private List<Map<String, Object>> toMap(List<PropFindResponse> props, Map<QName, String> aliases) {
            List<Map<String, Object>> list = new ArrayList<>();
            Object val;
            for (PropFindResponse prop : props) {
                Map<String, Object> map = new HashMap<>();
                list.add(map);
                for (Entry<QName, ValueAndType> p : prop.getKnownProperties().entrySet()) {
                    String name = aliases.get(p.getKey());
                    if (name == null) {
                        name = p.getKey().getLocalPart();
                    }
                    val = p.getValue().getValue();
                    map.put(name, val);
                }
            }
            return list;
        }
    }
}
