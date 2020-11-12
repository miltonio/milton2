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
public class StandardMessageImpl implements StandardMessage {

    private final static Logger log = LoggerFactory.getLogger(StandardMessageImpl.class);

    private MailboxAddress from;
    private MailboxAddress replyTo;
    private List<MailboxAddress> to = new ArrayList<>();
    private List<MailboxAddress> cc = new ArrayList<>();
    private List<MailboxAddress> bcc = new ArrayList<>();
    private String subject;
    private String html;
    private String text;
    private final List<Attachment> attachments = new ArrayList<>();
    private List<StandardMessage> attachedMessages = new ArrayList<>();
    private int size;
    private String disposition;
    private String encoding;
    private String contentLanguage;
    private Map<String, String> headers;

    @Override
    public List<StandardMessage> getAttachedMessages() {
        return attachedMessages;
    }

    @Override
    public void setAttachedMessages(List<StandardMessage> attachedMessages) {
        this.attachedMessages = attachedMessages;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public MailboxAddress getFrom() {
        return from;
    }

    public int size() {
        return size;
    }

    @Override
    public List<Attachment> getAttachments() {
        return attachments;
    }

    @Override
    public void setFrom(MailboxAddress from) {
        this.from = from;
    }

    @Override
    public MailboxAddress getReplyTo() {
        return replyTo;
    }

    @Override
    public void setReplyTo(MailboxAddress replyTo) {
        this.replyTo = replyTo;
    }

    @Override
    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public String getHtml() {
        return html;
    }

    @Override
    public void setHtml(String html) {
        this.html = html;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    @Override
    public String getDisposition() {
        return disposition;
    }

    @Override
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    @Override
    public String getContentLanguage() {
        return contentLanguage;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public List<MailboxAddress> getTo() {
        return to;
    }

    @Override
    public void setTo(List<MailboxAddress> to) {
        this.to = to;
    }

    @Override
    public List<MailboxAddress> getCc() {
        return cc;
    }

    @Override
    public void setCc(List<MailboxAddress> cc) {
        this.cc = cc;
    }

    @Override
    public List<MailboxAddress> getBcc() {
        return bcc;
    }

    @Override
    public void setBcc(List<MailboxAddress> bcc) {
        this.bcc = bcc;
    }

    /**
     *
     * @return - creates and returns a new instance of StandardMesssage suitable
     * for use as an attached message
     */
    @Override
    public StandardMessageImpl instantiateAttachedMessage() {
        return new StandardMessageImpl();
    }

    @Override
    public void addAttachment(String name, String ct, String contentId, String disposition, InputStream in) {
        FileSystemAttachment att = new FileSystemAttachment(name, ct, in, contentId);
        this.attachments.add(att);
    }

}
