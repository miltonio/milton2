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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class StandardMessageFactoryImpl implements StandardMessageFactory {

    private final static Logger log = LoggerFactory.getLogger(StandardMessageFactoryImpl.class);

    @Override
    public void toStandardMessage(MimeMessage mm, StandardMessage sm) {
        try {
            sm.setFrom(findFromAddress(mm));
            sm.setReplyTo(findReplyTo(mm));
            sm.setSubject(findSubject(mm));
            sm.setDisposition(mm.getDisposition());
            sm.setEncoding(mm.getEncoding());
            sm.setContentLanguage(findContentLanguage(mm.getContentLanguage()));

            sm.setTo(findRecips(mm, RecipientType.TO));
            sm.setCc(findRecips(mm, RecipientType.CC));
            sm.setBcc(findRecips(mm, RecipientType.BCC));
            sm.setSize(mm.getSize());
            Map<String, String> headers = findHeaders(mm);
            sm.setHeaders(headers);

            Object o = mm.getContent();
            if (o instanceof String) {
                String text = (String) o;
                log.debug( "text: " + text );

                sm.setText(text);
            } else if (o instanceof MimeMultipart) {
                MimeMultipart multi = (MimeMultipart) o;
                populateMultiPart(multi, sm);

            } else {
                log.warn("Unknown content type: " + o.getClass() + ". expected string or MimeMultipart");
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void populateMultiPart(MimeMultipart multi, StandardMessage sm) throws IOException, MessagingException {
        log.debug( "populateMultiPart: content type: " + multi.getContentType());
        for (int i = 0; i < multi.getCount(); i++) {
            BodyPart bp = multi.getBodyPart(i);
            String disp = bp.getDisposition();
            if ((disp != null) && (disp.equals(Part.ATTACHMENT) || disp.equals(Part.INLINE))) {
                addAttachment(sm, bp);
            } else {
                String ct = bp.getContentType();
                if (ct.contains("html")) {
                    if (sm.getHtml() == null) {
                        sm.setHtml("");
                    }
                    String s = sm.getHtml() + getStringContent(bp);
                    sm.setHtml(s);
                } else if (ct.contains("text")) {
                    if (sm.getText() == null) {
                        sm.setText("");
                    }
                    String s = sm.getText() + getStringContent(bp);
                    sm.setText(s);
                } else if (ct.contains("multipart")) {
                    Object subMessage = bp.getContent();
                    if (subMessage instanceof MimeMultipart) {
                        MimeMultipart child = (MimeMultipart) subMessage;
                        StandardMessage smSub;
                        if (ct.contains("related") || ct.contains("alternative") || ct.contains("mixed")) { // accumulate content into current message
                            smSub = sm;
                        } else { // otherwise, treat it as an attached message
                            smSub = sm.instantiateAttachedMessage();
                            sm.getAttachedMessages().add(smSub);
                        }
                        populateMultiPart(child, smSub);
                    } else {
                        log.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11 unknown sub message type");
                    }

                } else {
                    addAttachment(sm, bp);
                }
            }
        }
    }

    protected void addAttachment(StandardMessage sm, BodyPart bp) {
        InputStream in = null;
        try {
            String name = bp.getFileName();
            if (name == null) {
                name = System.currentTimeMillis() + "";
            }
            String ct = bp.getContentType();
            log.debug( "attachment content type: " + ct);
            String[] contentIdArr = bp.getHeader("Content-ID");
            String contentId = null;
            if (contentIdArr != null && contentIdArr.length > 0) {
                contentId = contentIdArr[0];
                contentId = Utils.parseContentId(contentId);
            }
            in = bp.getInputStream();
            sm.addAttachment(name, ct, contentId, in);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        } finally {
            Utils.close(in);
        }
    }


    Map<String, String> findHeaders(MimeMessage mm) {
        try {
            Map<String, String> map = new HashMap<String, String>();
            Enumeration en = mm.getAllHeaders();
            while (en.hasMoreElements()) {
                Object o = en.nextElement();
                Header header = (Header) o;
                map.put(header.getName(), header.getValue());
            }
            return map;
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    void fillRecipients(List<MailboxAddress> to, Address[] recipients) {
        if (recipients == null) {
            return;
        }
        for (Address a : recipients) {
            MailboxAddress ma = MailboxAddress.parse(a.toString());
            to.add(ma);
        }
    }

    void fillContentLanguage(String contentLanguage, MimeMessage mm) {
        try {
            if (contentLanguage == null) {
                return;
            }
            String[] arr = {contentLanguage};
            mm.setContentLanguage(arr);
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void fillContent(StandardMessage sm, Part message) throws MessagingException {
        System.out.println("StandardMessageFactoryImpl - fillContent");
        if (isText(sm)) {
            if (isHtml(sm)) {
                if (hasAttachments(sm)) {
                    // mixed, then alternate, related for html
                    MimeMultipart multipart = new MimeMultipart("mixed");
                    message.setContent(multipart);
                    addTextAndHtmlToMime(multipart, sm);
                    addAttachmentsToMime(multipart, sm);
                } else {
                    log.debug("text and html. no attachments");
                    addTextAndHtmlToMime(message, sm);
                }
            } else {
                if (hasAttachments(sm)) {
                    // just text and attachments
                    MimeMultipart multipart = new MimeMultipart("mixed");
                    message.setContent(multipart);
                    addTextToMime(multipart, sm);
                    addAttachmentsToMime(multipart, sm);
                } else {
                    // no html, no attachments
                    message.setContent(sm.getText(), "text/plain");
                }
            }
        } else {
            if (isHtml(sm)) {
                if (hasAttachments(sm)) {
                    // no text, but has html and attachments, so must do mixed
                    MimeMultipart multipart = new MimeMultipart("mixed");
                    message.setContent(multipart);
                    addHtmlToMime(multipart, sm);
                    addAttachmentsToMime(multipart, sm);
                } else {
                    // html only, no text or attachments
                    addHtmlToMime(message, sm);
                }
            } else {
                if (hasAttachments(sm)) {
                    // only attachments
                    MimeMultipart multipart = new MimeMultipart("mixed");
                    message.setContent(multipart);
                    addAttachmentsToMime(multipart, sm);
                } else {
                    // no text, no html, no attachments - no content
                    message.setContent("", "text/plain");
                }
            }
        }
    }

    protected boolean isText(StandardMessage sm) {
        return sm.getText() != null && sm.getText().length() > 0;
    }

    protected boolean isHtml(StandardMessage sm) {
        return sm.getHtml() != null && sm.getHtml().length() > 0;
    }

    protected boolean hasAttachments(StandardMessage sm) {
        return (sm.getAttachments() != null && sm.getAttachments().size() > 0) || (sm.getAttachedMessages() != null && sm.getAttachedMessages().size() > 0);
    }

    protected void fillReplyTo(StandardMessage sm, MimeMessage mm) {
        try {
            MailboxAddress ma = sm.getReplyTo();
            if (ma == null) {
                return;
            }
            Address[] addresses = new Address[1];
            addresses[0] = ma.toInternetAddress();
            mm.setReplyTo(addresses);
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void addAttachmentToMime(MimeMultipart multipart, Attachment att) throws MessagingException {
        System.out.println("StandardMessageFactoryImpl - addAttachmentToMime2 - " + att.getContentId());
        MimeBodyPart bp = new MimeBodyPart();

        DataSource fds = new AttachmentReadingDataSource(att);
        bp.setDataHandler(new DataHandler(fds));
        bp.setHeader("Content-ID", att.getContentId());
        bp.setDisposition(att.getDisposition());
        bp.setFileName(att.getName());

        multipart.addBodyPart(bp);
    }

    /**
     * Adds non inline attachments to the multiparts
     *
     * @param multipart
     * @param sm
     */
    private void addAttachmentsToMime(MimeMultipart multipart, StandardMessage sm) throws MessagingException {
        System.out.println("StandardMessageFactoryImpl - addAttachmentsToMime1");
        if (sm.getAttachments() != null && sm.getAttachments().size() > 0) {
            for (Attachment att : sm.getAttachments()) {
                if( !isInline(att) ) {
                    System.out.println("StandardMessageFactoryImpl - addAttachmentToMime1");
                    addAttachmentToMime(multipart, att);
                } else {
                    System.out.println("StandardMessageFactoryImpl - is inline so ignore");
                }
            }
        }
        if (sm.getAttachedMessages() != null && sm.getAttachedMessages().size() > 0) {
            for (StandardMessage smAttached : sm.getAttachedMessages()) {
                MimeBodyPart bp = new MimeBodyPart();
                fillContent(smAttached, bp);
                multipart.addBodyPart(bp);
            }
        }

    }

    private void addHtmlToMime(MimeMultipart multipart, StandardMessage sm) throws MessagingException {
        BodyPart bp = new MimeBodyPart();
        multipart.addBodyPart(bp);
        addHtmlToMime(bp, sm);
    }

    private void addHtmlToMime(Part part, StandardMessage sm) throws MessagingException {

			// need to use a javax.activation.DataSource (!) to set a text
			// with content type "text/html"
//			part.setDataHandler(new DataHandler(
//			    new DataSource() {
//						public InputStream getInputStream() throws IOException {
//							return new ByteArrayInputStream(encoding != null ? text.getBytes(encoding) : text.getBytes());
//						}
//						public OutputStream getOutputStream() throws IOException {
//							throw new UnsupportedOperationException("Read-only javax.activation.DataSource");
//						}
//						public String getContentType() {
//							return "text/html";
//						}
//						public String getName() {
//							return "text";
//						}
//			    }
//			));


        List<Attachment> htmlInline = findInlineAttachments(sm);
        if (htmlInline == null || htmlInline.isEmpty()) {
            part.setContent(sm.getHtml(), "text/html");
        } else {
            MimeMultipart related = new MimeMultipart("related");
            part.setContent(related);
            BodyPart bpHtml = new MimeBodyPart();
            bpHtml.setContent(sm.getHtml(), "text/html");
            related.addBodyPart(bpHtml);
            for (Attachment att : htmlInline) {
                addAttachmentToMime(related, att);
            }
        }
    }

    private void addTextAndHtmlToMime(MimeMultipart multipart, StandardMessage sm) throws MessagingException {
        MimeMultipart alternate = createTextAndHtml(sm);
        MimeBodyPart bpAlternate = new MimeBodyPart();
        bpAlternate.setContent(alternate);
        multipart.addBodyPart(bpAlternate);
    }

    private void addTextAndHtmlToMime(Part message, StandardMessage sm) throws MessagingException {
        MimeMultipart alternate = createTextAndHtml(sm);
        message.setContent(alternate);
    }

    private MimeMultipart createTextAndHtml(StandardMessage sm) throws MessagingException {
        MimeMultipart alternate = new MimeMultipart("alternative");
        MimeBodyPart bpAlternate = new MimeBodyPart();
        bpAlternate.setContent(alternate);

        addTextToMime(alternate, sm);
        addHtmlToMime(alternate, sm);
        return alternate;
    }

    private void addTextToMime(MimeMultipart multipart, StandardMessage sm) throws MessagingException {
        BodyPart bp = new MimeBodyPart();
        bp.setContent(sm.getText(), "text/plain");
        multipart.addBodyPart(bp);
    }

    private void fillBCC(List<MailboxAddress> bcc, MimeMessage mm) {
        fillRecipients(bcc, mm, RecipientType.BCC);
    }

    private void fillCC(List<MailboxAddress> cc, MimeMessage mm) {
        fillRecipients(cc, mm, RecipientType.CC);
    }

    private void fillRecipients(List<MailboxAddress> list, MimeMessage mm, RecipientType type) {
        for (MailboxAddress ma : list) {
            try {
                mm.addRecipient(type, ma.toInternetAddress());
            } catch (MessagingException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void fillTo(List<MailboxAddress> to, MimeMessage mm) {
        fillRecipients(to, mm, RecipientType.TO);
    }

    /**
     *
     * @param sm
     * @return - a list of attachments which have a non empty Content-ID header
     */
    private List<Attachment> findInlineAttachments(StandardMessage sm) {
        if( sm.getAttachments() == null ) return null;
        List<Attachment> list = new ArrayList<Attachment>();
        for( Attachment att : sm.getAttachments() ) {
            if( isInline(att)) {
                list.add(att);
            }
        }
        return list;
    }

    private MailboxAddress findReplyTo(MimeMessage mm) {
        try {
            return findSingleAddress(mm.getReplyTo());
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    MailboxAddress findFromAddress(MimeMessage mm) {
        try {
            return findSingleAddress(mm.getFrom());
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    MailboxAddress findSingleAddress(Address[] addresses) {
        if (addresses == null || addresses.length == 0) {
            return null;
        }
        return MailboxAddress.parse(addresses[0].toString());
    }

    String findContentLanguage(String[] arr) {
        if (arr == null || arr.length == 0) {
            return null;
        }
        return arr[0];
    }

    String findSubject(MimeMessage mm) {
        try {
            return mm.getSubject();
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    String getStringContent(BodyPart bp) {
        String text;
        try {
            Object o2 = bp.getContent();
            if (o2 == null) {
                text = "";
            } else if (o2 instanceof String) {
                text = (String) o2;
            } else {
                log.warn("Unknown content type: " + o2.getClass());
                text = o2.toString();
            }
            log.debug( "getStringContent: " + text);
            return text;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    List<MailboxAddress> findRecips(MimeMessage mm, RecipientType type) {
        try {
            Address[] recips = mm.getRecipients(type);
            List<MailboxAddress> list = new ArrayList<MailboxAddress>();
            if (recips != null) {
                for (Address a : recips) {
                    MailboxAddress mba = MailboxAddress.parse(a.toString());
                    list.add(mba);
                }
            }
            return list;
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public MimeMessage toMimeMessage(StandardMessage sm, Session session) {
        MimeMessage mm = new MimeMessage(session);
        toMimeMessage(sm, mm);
        return mm;
    }

    @Override
    public void toMimeMessage(StandardMessage sm, MimeMessage mm) {
        System.out.println("StandardMessageFactoryImpl - toMimeMessage");
        try {
            //mm.setS
            mm.setFrom(sm.getFrom().toInternetAddress());
            mm.setSender(sm.getFrom().toInternetAddress());
            fillReplyTo(sm, mm);
            fillTo(sm.getTo(), mm);
            fillCC(sm.getCc(), mm);
            fillBCC(sm.getBcc(), mm);
            mm.setSubject(sm.getSubject());
            mm.setDisposition(sm.getDisposition());
            fillContentLanguage(sm.getContentLanguage(), mm);
            fillContent(sm, mm);
            // todo: set headers?
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean isInline(Attachment att) {
        return att.getContentId() != null && att.getContentId().length() > 2;
    }


    public class AttachmentReadingDataSource implements DataSource {

        final Attachment att;

        public AttachmentReadingDataSource(Attachment att) {
            this.att = att;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            System.out.println("AttachmentReadingDataSource - getInputStream - " + att.getName());
            return att.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getContentType() {
            log.debug( "attachment conte type: " + att.getContentType());
            return att.getContentType();
        }

        @Override
        public String getName() {
            return att.getName();
        }
    }
}
