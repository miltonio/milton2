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
 * Useful for wrapping a standard message. You can then modify properties of
 * this object which will override, but not affect, the wrapped object
 *
 */
public class StandardMessageWrapper implements StandardMessage {

    private final StandardMessage wrapped;

    private String subject;
    private MailboxAddress from;
    private MailboxAddress replyTo;
    private String html;
    private String text;
    private List<MailboxAddress> to;
    private List<MailboxAddress> cc;
    private List<MailboxAddress> bcc;

    public StandardMessageWrapper(StandardMessage wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void addAttachment(String name, String ct, String contentId, String disposition, InputStream in) {
        throw new UnsupportedOperationException("Not supported for wrapper.");
    }

    @Override
    public List<StandardMessage> getAttachedMessages() {
        return wrapped.getAttachedMessages();
    }

    @Override
    public void setAttachedMessages(List<StandardMessage> attachedMessages) {
        throw new UnsupportedOperationException("Not supported for wrapper.");
    }

    @Override
    public String getSubject() {
        if (subject != null) {
            return subject;
        } else {
            return wrapped.getSubject();
        }
    }

    @Override
    public MailboxAddress getFrom() {
        if (from != null) {
            return from;
        } else {
            return wrapped.getFrom();
        }
    }

    @Override
    public List<Attachment> getAttachments() {
        return wrapped.getAttachments();
    }

    @Override
    public void setFrom(MailboxAddress from) {
        this.from = from;
    }

    @Override
    public MailboxAddress getReplyTo() {
        return wrapped.getReplyTo();
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
        if (this.html != null) {
            return html;
        } else {
            return wrapped.getHtml();
        }
    }

    @Override
    public void setHtml(String html) {
        this.html = html;
    }

    @Override
    public String getText() {
        if (this.text != null) {
            return text;
        } else {
            return wrapped.getText();
        }
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int getSize() {
        return wrapped.getSize();
    }

    @Override
    public void setSize(int size) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setDisposition(String disposition) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDisposition() {
        return wrapped.getDisposition();
    }

    @Override
    public void setEncoding(String encoding) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getEncoding() {
        return wrapped.getEncoding();
    }

    @Override
    public void setContentLanguage(String contentLanguage) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getContentLanguage() {
        return wrapped.getContentLanguage();
    }

    @Override
    public Map<String, String> getHeaders() {
        return wrapped.getHeaders();
    }

    @Override
    public void setHeaders(Map<String, String> headers) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<MailboxAddress> getTo() {
        if (to != null) {
            return to;
        } else {
            return wrapped.getTo();
        }
    }

    @Override
    public void setTo(List<MailboxAddress> to) {
        this.to = to;
    }

    @Override
    public List<MailboxAddress> getCc() {
        if (cc != null) {
            return cc;
        } else {
            return wrapped.getCc();
        }
    }

    @Override
    public void setCc(List<MailboxAddress> cc) {
        this.cc = cc;
    }

    @Override
    public List<MailboxAddress> getBcc() {
        if (this.bcc != null) {
            return this.bcc;
        } else {
            return wrapped.getBcc();
        }
    }

    @Override
    public void setBcc(List<MailboxAddress> bcc) {
        this.bcc = bcc;
    }

    @Override
    public StandardMessage instantiateAttachedMessage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
