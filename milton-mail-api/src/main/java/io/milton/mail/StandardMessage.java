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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Interface which describes a standard message. This is a representation of an
 * email which can have text, html and attachments. Use the
 * StandardMessageFactory to populate these from a MimeMessage and to convert
 * back again
 *
 * Note that an implementation class may often wish to implement this and
 * MessageResource
 */
public interface StandardMessage {

    /**
     * Add a new attachment to this message. This may be called multiple times
     * during parsing, but after the subject and from fields are set.
     *
     * @param name
     * @param ct
     * @param contentId
     * @param disposition
     * @param in
     */
    void addAttachment(String name, String ct, String contentId, String disposition, InputStream in);

    List<StandardMessage> getAttachedMessages();

    void setAttachedMessages(List<StandardMessage> attachedMessages);

    String getSubject();

    MailboxAddress getFrom();

    List<Attachment> getAttachments();

    void setFrom(MailboxAddress from);

    MailboxAddress getReplyTo();

    void setReplyTo(MailboxAddress replyTo);

    void setSubject(String subject);

    String getHtml();

    void setHtml(String html);

    String getText();

    void setText(String text);

    int getSize();

    void setSize(int size);

    void setDisposition(String disposition);

    String getDisposition();

    void setEncoding(String encoding);

    String getEncoding();

    void setContentLanguage(String contentLanguage);

    String getContentLanguage();

    Map<String, String> getHeaders();

    void setHeaders(Map<String, String> headers);

    List<MailboxAddress> getTo();

    void setTo(List<MailboxAddress> to);

    List<MailboxAddress> getCc();

    void setCc(List<MailboxAddress> cc);

    List<MailboxAddress> getBcc();

    void setBcc(List<MailboxAddress> bcc);

    /**
     *
     * @return - creates and returns a new instance of StandardMesssage suitable
     * for use as an attached message
     */
    StandardMessage instantiateAttachedMessage();

}
