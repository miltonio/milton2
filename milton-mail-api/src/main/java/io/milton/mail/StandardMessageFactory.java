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

import javax.mail.internet.MimeMessage;

/**
 *  A service to copy information from a MimeMessage to a
 *  StandardMessage and vice versa
 */
public interface  StandardMessageFactory {

    /**
     * copy information from the given standard messsage into a new MimeMessage
     * object, which will be constructed on the given Session
     *
     * @param sm
     * @param mm - 
     */
    void toMimeMessage(StandardMessage sm, MimeMessage mm);

    /**
     * Copy information from the MimeMessage into the standard message
     *
     * @param mm - the MimeMessage which is the source of information
     * @param sm - the standard message to be populated    
     */
    void toStandardMessage(MimeMessage mm, StandardMessage sm);
}
