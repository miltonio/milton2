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

package io.milton.http.http11;

import io.milton.http.Range;
import io.milton.resource.ReplaceableResource;
import java.io.InputStream;

/**
 * A resource which, as well as being completely replaceable, can have its content
 * partially replaced. ie individual ranges can be set
 *
 * While PutHandler will do this for you even if you don't implement this interface,
 * the approach used might not be efficient. Ie milton will retrieve your complete
 * content, then insert the update, then set the entire content back again like
 * a regular put.
 *
 * By implementing this interface you have control over how you manage the
 * updated resource.
 *
 *
 * @author brad
 */
public interface PartialllyUpdateableResource extends ReplaceableResource {
    /**
     * Update the content with the date in the given inputstream, affecting
     * only those bytes in the given range.
     *
     * Note that the range positions are zero-based, so the first byte is 0
     *
     * @param range - the range to update
     * @param in - the inputstream containing the data
     */
    void replacePartialContent(Range range, InputStream in);
}
