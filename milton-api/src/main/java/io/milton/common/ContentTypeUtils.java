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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class ContentTypeUtils {

    private static Logger log = LoggerFactory.getLogger(ContentTypeUtils.class);
    private static final ContentTypeService contentTypeService = new DefaultContentTypeService(); // will load props file ;


    public static String findContentTypes(String name) {
        List<String> list = contentTypeService.findContentTypes(name);
        return buildContentTypeText(list);
    }

    public static String findContentTypes(File file) {
        return buildContentTypeText(contentTypeService.findContentTypes(file.getName()));
    }

    public static String findAcceptableContentType(String canProvide, String accepts) {
        return contentTypeService.getPreferedMimeType(accepts, toList(canProvide));
    }

    public static String findAcceptableContentTypeForName(String name, String accepts) {
        String canProvide = findContentTypes(name);
        List<String> canProvideList = toList(canProvide);
        String s = contentTypeService.getPreferedMimeType(accepts, canProvideList);
        return s;
    }
    
    
    private static String buildContentTypeText(List<String> mimeTypes) {
        return Utils.toCsv(mimeTypes);
    }

    public static List<String> toList(String s) {
        List<String> list = new ArrayList<String>();
        if( s != null ) {
            for( String x : s.split(",")) {
                x = x.trim();
                list.add(x);
            }
        }
        return list;
    }
}
