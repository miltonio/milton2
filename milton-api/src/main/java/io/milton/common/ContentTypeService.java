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

import java.util.List;

/**
 * Interface provides handful methods to work with mime types.
 *
 * @author brad
 */
public interface ContentTypeService {

    /**
     * Finds a content type by file name/extension.
     * @param name - file name or extension.
     * @return List of matched content types.
     */
    List<String> findContentTypes(String name);

    /**
     * Returns preferred mime type.
     * @param accept - content type which client accept.
     * @param canProvide - content types which server can provide.
     * @return content type which is accepted by client and supported by server, null otherwise.
     */
    String getPreferedMimeType(String accept, List<String> canProvide);

    /**
     * Returns preferred mime type.
     * @param accept - content types which client accept.
     * @param canProvide - content types which server can provide.
     * @return first content type which is accepted by client and supported by server, null otherwise.
     */
    String getPreferedMimeType(List<String> accept, List<String> canProvide);

}
