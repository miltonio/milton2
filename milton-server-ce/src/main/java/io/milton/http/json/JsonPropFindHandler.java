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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        log.debug("sendContent: " + encodedUrl);
        JsonConfig cfg = new JsonConfig();
        cfg.setIgnoreTransientFields(true);
        cfg.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);

        JSON json;
        Writer writer = new PrintWriter(out);
        String[] arr;
        if (propertyBuilder == null) {
            if (wrappedResource instanceof CollectionResource) {
                List<? extends Resource> children = ((CollectionResource) wrappedResource).getChildren();
                json = JSONSerializer.toJSON(toSimpleList(children), cfg);
            } else {
                json = JSONSerializer.toJSON(toSimple(wrappedResource), cfg);
            }
        } else {
            // use propfind handler
            String sFields = params.get("fields");
            Set<QName> fields = new HashSet<QName>();
            Map<QName, String> aliases = new HashMap<QName, String>();
            if (sFields != null && sFields.length() > 0) {
                arr = sFields.split(",");
                for (String s : arr) {
                    parseField(s, fields, aliases);
                }
            }

            String sDepth = params.get("depth");
            int depth = 1;
            if (sDepth != null && sDepth.trim().length() > 0) {
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

            List<Map<String, Object>> list = helper.toMap(props, aliases);
            json = JSONSerializer.toJSON(list, cfg);
        }
        json.write(writer);
        writer.flush();
    }
	
	private Set<Property> toProperties(Set<QName> set) {
		Set<Property> props = new HashSet<Property>();
		for(QName n : set ) {
			props.add(new Property(n, null));
		}
		return props;
	}	

    /**
     * Parse the given field and populate the given maps
     *
     * A field may be in the following forms
     * - foo
     * - DAV:foo
     * - DAV:foo>bar
     * - foo>bar
     *
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
        List<SimpleResource> simpleList = new ArrayList<SimpleResource>(list.size());
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
     *
     * If the given where argument starts with ! the condition is negated
     *
     * @param results
     * @param where
     */
    private void filterResults(List<PropFindResponse> results, String where) {
        if (where != null && where.length() > 0) {
            boolean negate = where.startsWith("!");
            if (negate) {
                where = where.substring(1);
            }
            ValueAndType prop;
            QName qnWhere = parseQName(where);
            Iterator<PropFindResponse> it = results.iterator();
            boolean removeValue = negate;
            while (it.hasNext()) {
                PropFindResponse result = it.next();
                boolean isTrue = eval(qnWhere, result);
                // eg !iscollection for a folder -> false == false = true, so remove
                // eg !iscollection for a file -> true == false = false, dont remove
                // eg iscollection for a folder -> false == true = false, so dont remove
                if (isTrue == removeValue) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Find a boolean value from the given propery name on the propfind
     * result.
     *
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
            if (val != null && val instanceof Boolean) {
                Boolean b = (Boolean) val;
                return b;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    public class SimpleResource {

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

    class Helper {

        private List<Map<String, Object>> toMap(List<PropFindResponse> props, Map<QName, String> aliases) {
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            Object val;
            for (PropFindResponse prop : props) {
                Map<String, Object> map = new HashMap<String, Object>();
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
