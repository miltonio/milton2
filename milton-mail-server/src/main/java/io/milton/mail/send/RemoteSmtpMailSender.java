package io.milton.mail.send;

import io.milton.mail.StandardMessage;
import io.milton.mail.StandardMessageFactory;
import io.milton.mail.StandardMessageFactoryImpl;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends emails via a remote SMTP server. This is often useful if you're email
 * server is running in a server which is blacklisted by spam lists, so you need
 * to relay through a trusted email server.
 *
 * @author brad
 */
public class RemoteSmtpMailSender implements MailSender {

    private final static Logger log = LoggerFactory.getLogger(RemoteSmtpMailSender.class);
    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private boolean started;
    private boolean useSsl = false;

    public RemoteSmtpMailSender(String host, String user, String password, int port) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.port = port;
    }

    @Override
    public void sendMail(String from, String fromPersonal, List<String> to, String replyTo, String subject, String text) {
        if (!started) {
            throw new RuntimeException("This mail sender is stopped");
        }
        try {
            MimeMessage mm = new MimeMessage(getSession());
            mm.setSubject(subject);
            InternetAddress ia;
            if (fromPersonal == null) {
                ia = new InternetAddress(from);
            } else {
                ia = new InternetAddress(from, fromPersonal);
            }
            mm.setFrom(ia);
            Address[] add = new Address[1];
            add[0] = new InternetAddress(replyTo);
            mm.setReplyTo(add);
            for (String sTo : to) {
                if (sTo != null && sTo.length() > 0) {
                    InternetAddress recip = null;
                    try {
                        recip = new InternetAddress(sTo);
                    } catch (AddressException addressException) {
                        throw new RuntimeException("Couldnt parse email address: " + sTo, addressException);
                    }
                    mm.addRecipient(RecipientType.TO, recip);
                }
            }
            mm.setContent(text, "text/plain");
            sendMail(mm);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        } catch (MessagingException messagingException) {
            throw new RuntimeException(messagingException);
        }
    }

    @Override
    public void sendMail(MimeMessage mm) {
        if (!started) {
            throw new RuntimeException("This mail sender is stopped");
        }
        try {
            log.debug("sending to: " + host);
            Transport tr;
            if (useSsl) {
                tr = getSession().getTransport("smtps");
            } else {
                tr = getSession().getTransport("smtp");
            }
            tr.connect(host, port, user, password);
            mm.saveChanges();
            tr.sendMessage(mm, mm.getAllRecipients());
            tr.close();
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void sendMail(StandardMessage sm) {
        StandardMessageFactory smf = new StandardMessageFactoryImpl();
        MimeMessage mm = newMessage();
        smf.toMimeMessage(sm, mm);
        sendMail(mm);
    }

    @Override
    public Session getSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        if (useSsl) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        if (log.isTraceEnabled()) {
            props.put("mail.debug", "true");
        }

        Session session = Session.getInstance(props, null);
        return session;
    }

    @Override
    public MimeMessage newMessage(MimeMessage mm) {
        try {
            return new MySmtpMessage(getSession(), mm);
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public MimeMessage newMessage() {
        return new MimeMessage(getSession());
    }

    @Override
    public void start() {
        log.debug("started the service");
        this.started = true;
    }

    @Override
    public void stop() {
        this.started = false;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public void setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
    }
}
