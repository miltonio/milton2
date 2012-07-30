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
package io.milton.httpclient;

import io.milton.http.DateUtils;
import java.util.*;
import javax.xml.namespace.QName;
import org.jdom.Element;
import org.jdom.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class PropFindResponse {

    private static final Logger log = LoggerFactory.getLogger(PropFindResponse.class);
    private final String name;
    private final String href;
    private final boolean collection;
    private Map<QName, Object> properties = new HashMap<QName, Object>();

    public PropFindResponse(Date serverDate, Element elResponse) {
        href = RespUtils.asString(elResponse, "href").trim();
        if (href.contains("/")) {
            String[] arr = href.split("[/]");
            if (arr.length > 0) {
                name = arr[arr.length - 1];
            } else {
                name = "";
            }
        } else {
            name = href;
        }
        List<Element> propElements = getFoundProps(elResponse);
        Element colElement = null;
        for (Object oElProp : propElements) {
            if (oElProp instanceof Element) {
                Element elProp = (Element) oElProp;
                String localName = elProp.getName();
                Namespace ns = elProp.getNamespace();
                QName qn = new QName(ns.getURI(), localName, ns.getPrefix());
                if (localName.equals("resourcetype")) {
                    colElement = elProp.getChild("collection", RespUtils.NS_DAV);  
                } else if (localName.equals("lockdiscovery")) {
                    Element elActiveLock = elProp.getChild("activelock", RespUtils.NS_DAV);
                    String token;
                    String owner;
                    if (elActiveLock != null) {
                        owner = RespUtils.asString(elActiveLock, "owner");
                        Element elToken = elActiveLock.getChild("locktoken", RespUtils.NS_DAV);
                        if (elToken != null) {
                            String t = RespUtils.asString(elToken, "href");
                            if (t != null && t.contains(":")) {
                                t = t.substring(t.indexOf(":"));
                            }
                            token = t;
                        } else {
                            token = null;
                        }
                    } else {
                        owner = null;
                        token = null;
                    }
                    LockDiscovery lock = new LockDiscovery(owner, token);
                    properties.put(qn, lock);
                } else {
                    String value = elProp.getText();
                    // Date properties should be adjusted for the difference between server and local time
                    if (localName.equals("creationdate") || localName.equals("getlastmodified")) {
                        try {
                            Date dt = DateUtils.parseWebDavDate(value);
                            if (serverDate != null) {
                                // calc difference and use that as delta on local time
                                long delta = serverDate.getTime() - dt.getTime();
                                dt = new Date(System.currentTimeMillis() - delta);
                            }
                            properties.put(qn, dt);
                            QName qnRaw = new QName(ns.getURI(), localName + "-raw", ns.getPrefix());
                            properties.put(qnRaw, value);
                        } catch (DateUtils.DateParseException e) {
                            log.warn("Couldnt parse date property: " + localName + " = " + value);
                        }
                    } else {
                        properties.put(qn, value);
                    }
                }
            }
        }
        collection = (colElement != null);
    }
    // getters for common properties

    public boolean isCollection() {
        return collection;
    }

    public String getHref() {
        return href;
    }

    public LockDiscovery getLock() {
        return (LockDiscovery) getDavProperty("lockdiscovery");
    }

    public String getName() {
        return name;
    }

    public Map<QName, Object> getProperties() {
        return properties;
    }

    public Object getDavProperty(String name) {
        QName qn = RespUtils.davName(name);
        return properties.get(qn);
    }

    public String getDisplayName() {
        String dn = (String) getDavProperty("displayname");
        if (dn == null) {
            dn = name;
        }
        return dn;
    }

    public Date getCreatedDate() {
        return (Date) getDavProperty("creationdate");
    }

    public Date getModifiedDate() {
        return (Date) getDavProperty("getlastmodified");
    }
    
    public String getEtag() {
        return (String) getDavProperty("getetag");
    }

    public String getContentType() {
        return (String) getDavProperty("getcontenttype");
    }

    public Long getContentLength() {
        String s = (String) getDavProperty("getcontentlength");
        if (s == null || s.trim().length() == 0) {
            return null;
        }
        return Long.parseLong(s);
    }

    public Long getQuotaAvailableBytes() {
        String s = (String) getDavProperty("quota-available-bytes");
        if (s == null || s.trim().length() == 0) {
            return null;
        }
        return Long.parseLong(s);
    }

    public Long getQuotaUsedBytes() {
        String s = (String) getDavProperty("quota-used-bytes");
        if (s == null || s.trim().length() == 0) {
            return null;
        }
        return Long.parseLong(s);
    }

    private List<Element> getFoundProps(Element elResponse) {
        for (Object olPropStat : elResponse.getChildren()) {
            if (olPropStat instanceof Element) {
                Element propStat = (Element) olPropStat;
                if (propStat.getName().equals("propstat")) {
                    Element elStatus = propStat.getChild("status", RespUtils.NS_DAV);
                    if (elStatus != null) {
                        String st = elStatus.getText();
                        if (st != null && st.contains("200")) {
                            Element elProps = propStat.getChild("prop", RespUtils.NS_DAV);
                            if (elProps != null) {
                                List<Element> list = new ArrayList<Element>();
                                for (Object oProp : elProps.getChildren()) {
                                    if (oProp instanceof Element) {
                                        Element elProp = (Element) oProp;
                                        list.add(elProp);
                                    }
                                }
                                return list;
                            }
                        }
                    }
                }
            }
        }
        return Collections.EMPTY_LIST;
    }

//        String dn = RespUtils.asString(el, "displayname");
//        displayName = (dn == null) ? name : dn;
//        createdDate = RespUtils.asString(el, "creationdate");
//        modifiedDate = RespUtils.asString(el, "getlastmodified");
//        contentType = RespUtils.asString(el, "getcontenttype");
//        contentLength = RespUtils.asLong(el, "getcontentlength");
//        quotaAvailableBytes = RespUtils.asLong(el, "quota-available-bytes");
//        quotaUsedBytes = RespUtils.asLong(el, "quota-used-bytes");
//        isCollection = RespUtils.hasChild(el, "collection");
//        Element elLockDisc = el.getChild("lockdiscovery", RespUtils.NS_DAV);
//        if (elLockDisc != null) {
//            Element elActiveLock = elLockDisc.getChild("activelock", RespUtils.NS_DAV);
//            if (elActiveLock != null) {
//                lockOwner = RespUtils.asString(elActiveLock, "owner");
//                Element elToken = elActiveLock.getChild("locktoken", RespUtils.NS_DAV);
//                if (elToken != null) {
//                    String t = RespUtils.asString(elToken, "href");
//                    if (t != null && t.contains(":")) {
//                        t = t.substring(t.indexOf(":"));
//                    }
//                    lockToken = t;
//                } else {
//                    lockToken = null;
//                }
//            } else {
//                lockOwner = null;
//                lockToken = null;
//            }
//        } else {
//            lockOwner = null;
//            lockToken = null;
//        }
//    
    public static class LockDiscovery {

        private String owner;
        private String token;

        public LockDiscovery(String owner, String token) {
            this.owner = owner;
            this.token = token;
        }

        public String getOwner() {
            return owner;
        }

        public String getToken() {
            return token;
        }
    }
}
