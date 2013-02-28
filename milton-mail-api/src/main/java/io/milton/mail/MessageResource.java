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
package io.milton.mail;

import java.io.OutputStream;

/**
 *  The minimal interface needed to support basic email functionality. The
 * interface allows a resource to identify its size, to be deleted, and to be
 *  written to a client
 */
public interface MessageResource {
    /**
     * physically deleteMessage the resource
     */
    void deleteMessage();

    /**
     *
     *
     * @return - the size of the message when formatted as a mime message
     */
    int getSize();

    /**
     * write the message in mime format to the given output stream
     *
     * this will usually be implemented as mimeMessage.writeTo(out);
     *
     * @param out
     */
    void writeTo(OutputStream out);
}
