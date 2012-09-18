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
package io.milton.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This implementation of ContentTypeService just uses a map of file extension
 * to mime types. It supports multiple mimetypes per extension, but does not support
 * qos.
 * 
 * The default constructor reads a properties file /mime-types.properties, which 
 * should be a series of lines in the form:
 * 
 * ext=contentType1,contentType2
 * 
 * Eg:
 * arj=application/arj,application/octet-stream
 * 
 * 
 *
 * @author brad
 */
public class DefaultContentTypeService implements ContentTypeService {

    private final Map<String, List<String>> mapOfContentTypes;

    public DefaultContentTypeService(Map<String, List<String>> mapOfContentTypes) {
        this.mapOfContentTypes = mapOfContentTypes;
    }

    public DefaultContentTypeService() {
        mapOfContentTypes = new ConcurrentHashMap<String, List<String>>();
        Properties p = new Properties();
        InputStream in = getClass().getResourceAsStream("/mime-types.properties");
        if (in != null) {
            try {
                p.load(in);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            for (String extension : p.stringPropertyNames()) {
                String v = p.getProperty(extension);
                String[] contentTypes = v.split(",");
                List<String> list = new ArrayList<String>();
                list.addAll(Arrays.asList(contentTypes));
                mapOfContentTypes.put(extension, list);
            }
        } else {
            throw new RuntimeException("Couldnt find properties file to load from classpath: mime-types.properties");
        }
    }

    @Override
    public List<String> findContentTypes(String name) {
        String ext = FileUtils.getExtension(name);
        if( ext == null || ext.length() == 0 ) {
            return null;
        }
        ext = ext.toLowerCase();
        return mapOfContentTypes.get(ext);
    }

    @Override
    public String getPreferedMimeType(String accept, List<String> canProvide) {
        if (canProvide == null) {
            return null;
        }
        if (canProvide.isEmpty()) {
            return null;
        }
        if (canProvide.size() == 1) {
            return canProvide.get(0);
        }
        if (accept == null || accept.length() == 0 || accept.equals("*/*")) {
            return canProvide.get(0);
        }

        // Drop the qos parameter, because we don't support it        
        // eg  text/html; q=0.4 -> text/html
        accept = stripQop(accept);

        String s = getBestMatch(accept, canProvide);
        if( s != null ) {
            return s;
        }
        if( canProvide.isEmpty() ) {
            return null;
        }
        String ct = canProvide.get(0);
        return stripQop(ct);
    }

    @Override
    public String getPreferedMimeType(List<String> accepts, List<String> canProvide) {
        if (canProvide == null) {
            return null;
        }
        if (canProvide.isEmpty()) {
            return null;
        }
        if (canProvide.size() == 1) {
            return canProvide.get(0);
        }
        if (accepts == null || accepts.isEmpty()) {
            return canProvide.get(0);
        }

        for (String accept : accepts) {
            accept = stripQop(accept);
            String best = getBestMatch(accept, canProvide);
            if( best != null ) {
                return best;
            }
        }
        return null;
    }

    private String stripQop(String s) {
        if (s.contains(";")) {
            int pos = s.indexOf(";");
            s = s.substring(0, pos);
        }
        return s;
    }

    /**
     * Simple implementation which just returns the first item in the canProvide
     * list which is the start of the accept parameter
     *
     * @param accept
     * @param canProvideList
     * @return
     */
    private String getBestMatch(final String accept, List<String> canProvideList) {
        for (String cp : canProvideList) {
            cp = stripQop(cp);
            if (cp.contains(accept) || cp.equals(accept) ) {
                return cp;
            }
        }
        return null;
    }
}
