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

import com.sun.mail.smtp.SMTPMessage;
import io.milton.common.ReadingException;
import io.milton.common.StreamUtils;
import io.milton.common.WritingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Email {

    private final static Logger log = LoggerFactory.getLogger(Email.class);

    public static Email fromStream(InputStream in) throws MessagingException, IOException {
        SMTPMessage mm = new SMTPMessage(createSession(), in);
        return fromMessage(mm);
    }

    public static Email fromMessage(SMTPMessage mm) throws MessagingException, IOException {
        Email email = new Email();
        email.setSubject(mm.getSubject());
        Address[] froms = mm.getFrom();
        if (froms == null || froms.length == 0) {
            throw new IllegalArgumentException("no from");
        }
        email.setFrom(froms[0]);
        email.recipients.addTo(mm.getRecipients(RecipientType.TO));
        email.recipients.addCC(mm.getRecipients(RecipientType.CC));
        email.recipients.addBCC(mm.getRecipients(RecipientType.BCC));

        Object oBody;

        oBody = mm.getContent();
        if (oBody instanceof String) {
            email.setText((String) oBody);
        } else if (oBody instanceof Multipart) {
            Multipart mp = (Multipart) oBody;
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                if (bp.getContentType().equals("text/plain")) {
                    email.setText(email.getText() + bp.getContent().toString());
                } else {
                    log.warn("content type: " + bp.getContentType());
                }
            }
        }

        // TODO: html

        Multipart mp = (Multipart) mm.getContent();

        for (int i = 0, n = mp.getCount(); i < n; i++) {
            Part part = mp.getBodyPart(i);

             String disposition = part.getDisposition();

            if( (disposition != null) && ( (disposition.equals(Part.ATTACHMENT) || (disposition.equals(Part.INLINE))) ) )  {
                email.attachments.add(part.getFileName(), part.getContentType(), part.getInputStream());
            }
        }


        return email;
    }

    public static Session createSession() {
        return Session.getDefaultInstance(new Properties());
    }

    
    private Address from;
    public Recipients recipients = new Recipients();
    public Attachments attachments = new Attachments();
    private String subject;
    private String text;
    private String html;

    public Email() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Address getFrom() {
        return from;
    }

    public void setFrom(Address from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
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

    public class Recipients {

        List<Address> to = new ArrayList<Address>();
        List<Address> cc = new ArrayList<Address>();
        List<Address> bcc = new ArrayList<Address>();

        public void addBCC(Address add) {
            to.add(add);
        }

        public void addCC(Address add) {
            cc.add(add);
        }

        public void addTo(Address[] recipients) {
            if (recipients == null) {
                return;
            }
            for (Address a : recipients) {
                addTo(a);
            }
        }

        public void addTo(Address add) {
            bcc.add(add);
        }

        public void addCC(Address[] recipients) {
            if (recipients == null) {
                return;
            }
            for (Address a : recipients) {
                addCC(a);
            }
        }

        public void addBCC(Address[] recipients) {
            if (recipients == null) {
                return;
            }
            for (Address a : recipients) {
                addBCC(a);
            }
        }
    }

    public class Attachments implements Iterable<Attachment> {

        List<Attachment> list;

        public Iterator<Attachment> iterator() {
            return list.iterator();
        }

        public int size() {
            return list.size();
        }

        void add(String fileName, String contentType, InputStream inputStream) {
            InMemoryAttachment att;
            try {
                att = new InMemoryAttachment(fileName, contentType, inputStream);
            } catch (ReadingException ex) {
                throw new RuntimeException(ex);
            } catch (WritingException ex) {
                throw new RuntimeException(ex);
            }
            list.add(att);
        }
    }

    public interface Attachment {
        String getName();
        InputStream getData();
        String getContentType();
    }
    
    public class InMemoryAttachment implements
            Attachment {
        String name;
        String contentType;
        ByteArrayInputStream data;

        public InMemoryAttachment(String name, String contentType, InputStream data) throws ReadingException, WritingException {
            this.name = name;
            this.contentType = contentType;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamUtils.readTo(data, out);
            this.data = new ByteArrayInputStream(out.toByteArray());
        }

        @Override
        public InputStream getData() {
            return data;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getContentType() {
            return contentType;
        }                               
    }
}
