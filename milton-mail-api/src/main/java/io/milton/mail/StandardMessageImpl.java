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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class StandardMessageImpl implements StandardMessage{

    
    private final static Logger log = LoggerFactory.getLogger(StandardMessageImpl.class);

    private MailboxAddress from;
    private MailboxAddress replyTo;
    private List<MailboxAddress> to = new ArrayList<MailboxAddress>();
    private List<MailboxAddress> cc = new ArrayList<MailboxAddress>();
    private List<MailboxAddress> bcc = new ArrayList<MailboxAddress>();
    private String subject;
    private String html;
    private String text;
    private List<Attachment> attachments = new ArrayList<Attachment>();
    private List<StandardMessage> attachedMessages = new ArrayList<StandardMessage>();
    private int size;
    private String disposition;
    private String encoding;
    private String contentLanguage;
    private Map<String,String> headers;

    public List<StandardMessage> getAttachedMessages() {
        return attachedMessages;
    }

    public void setAttachedMessages(List<StandardMessage> attachedMessages) {
        this.attachedMessages = attachedMessages;
    }

    

    public String getSubject() {
        return subject;
    }
    
    public MailboxAddress getFrom() {
        return from;
    }

    public int size() {
        return size;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setFrom(MailboxAddress from) {
        this.from = from;
    }

    public MailboxAddress getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(MailboxAddress replyTo) {
        this.replyTo = replyTo;
    }


    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public String getDisposition() {
        return disposition;
    }


    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    public String getContentLanguage() {
        return contentLanguage;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public List<MailboxAddress> getTo() {
        return to;
    }

    public void setTo(List<MailboxAddress> to) {
        this.to = to;
    }

    public List<MailboxAddress> getCc() {
        return cc;
    }

    public void setCc(List<MailboxAddress> cc) {
        this.cc = cc;
    }

    public List<MailboxAddress> getBcc() {
        return bcc;
    }

    public void setBcc(List<MailboxAddress> bcc) {
        this.bcc = bcc;
    }

    /**
     *
     * @return - creates and returns a new instance of StandardMesssage suitable
     * for use as an attached message
     */
    public StandardMessageImpl instantiateAttachedMessage() {
        StandardMessageImpl sub = new StandardMessageImpl();
        return sub;
    }

    public void addAttachment(String name, String ct, String contentId, InputStream in) {
        FileSystemAttachment att = new FileSystemAttachment(name, ct, in, contentId);
        this.attachments.add(att);
    }


    
}
