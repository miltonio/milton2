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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility which uses {@link DefaultContentTypeService} for mime types resolving.
 *
 * @author brad
 */
public class ContentTypeUtils {

    private ContentTypeUtils() {
    }

    private static final ContentTypeService contentTypeService = new DefaultContentTypeService(); // will load props file ;

    /**
     * Finds a content type by file name/extension.
     * @param name - file name or extension.
     * @return Comma separated list of matched content types.
     */
    public static String findContentTypes(String name) {
        List<String> list = contentTypeService.findContentTypes(name);
        return buildContentTypeText(list);
    }

    /**
     * Finds a content type by file name/extension.
     * @param file - file name or extension.
     * @return Comma separated list of matched content types.
     */
    public static String findContentTypes(File file) {
        return buildContentTypeText(contentTypeService.findContentTypes(file.getName()));
    }

    /**
     * Returns acceptable mime type.
     * @param canProvide - content type which server can provide.
     * @param accepts - content type which client accept.
     * @return content type which is accepted by client and supported by server, null otherwise.
     */
    public static String findAcceptableContentType(String canProvide, String accepts) {
        return contentTypeService.getPreferedMimeType(accepts, toList(canProvide));
    }

    /**
     * Returns acceptable mime type for file name.
     * @param name - file name.
     * @param accepts - content type which client accept.
     * @return content type which is accepted by client and supported by server, null otherwise.
     */
    public static String findAcceptableContentTypeForName(String name, String accepts) {
        String canProvide = findContentTypes(name);
        List<String> canProvideList = toList(canProvide);
        return contentTypeService.getPreferedMimeType(accepts, canProvideList);
    }

    /**
     * Converts comma separated string to list
     * @param string - comma separated string.
     * @return List.
     */
    public static List<String> toList(String string) {
        List<String> list = new ArrayList<>();
        if (string != null) {
            for (String x : string.split(",")) {
                x = x.trim();
                list.add(x);
            }
        }
        return list;
    }

    private static String buildContentTypeText(List<String> mimeTypes) {
        return Utils.toCsv(mimeTypes);
    }
}
