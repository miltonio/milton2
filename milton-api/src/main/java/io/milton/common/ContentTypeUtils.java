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
        if (s != null) {
            for (String x : s.split(",")) {
                x = x.trim();
                list.add(x);
            }
        }
        return list;
    }
}
