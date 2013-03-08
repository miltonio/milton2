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
 *
 * @author brad
 */
public interface Mailbox {

    /**
     * Required for SMTP and pop command in POP
     * 
     * @param password
     * @return 
     */
    boolean authenticate(String password);
    
    /**
     * Required for apop command in POP protocol
     * 
     * @param passwordHash
     * @return 
     */
    boolean authenticateMD5(byte[] passwordHash);

    /**
     * Locate the primary folder to deliver mail to
     * 
     * @return 
     */
    MessageFolder getInbox();
    
    /**
     * Locate the named folder
     * 
     * @param name
     * @return 
     */
    MessageFolder getMailFolder(String name);

    /**
     * 
     * @return - true iff this mailbox is enabled for sending and receiving emails
     */
    boolean isEmailDisabled();
    
    /**
     * Called when a message has been received for delivery to this user. The message
     * should be stored in the user's inbox
     * 
     * @param mm 
     */
    void storeMail(MimeMessage mm);

}
