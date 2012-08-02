/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

        return getBestMatch(accept, canProvide);
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
