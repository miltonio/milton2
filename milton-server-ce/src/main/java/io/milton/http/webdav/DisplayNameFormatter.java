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

import io.milton.resource.PropFindableResource;

/**
 * This interface serves to allow users of milton to implement different
 * display name strategies.
 *
 * The display name is completely arbitrary, ie it is not in any way necessarily
 * related to the actual name used to contruct the href.
 *
 * This class also serves as a mechanism for deciding whether to wrap the display
 * name in a CDATA element.
 *
 * @author brad
 */
public interface DisplayNameFormatter {
    /**
     * Generate the exact text to appear inside display name elements. No
     * further encoding of this text is applied when generating the xml.
     *
     * @param res
     * @return
     */
    String formatDisplayName(PropFindableResource res);
}
