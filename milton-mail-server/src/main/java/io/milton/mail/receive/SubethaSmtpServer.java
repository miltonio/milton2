
package io.milton.mail.receive;

import io.milton.mail.AcceptEvent;
import io.milton.mail.DeliverEvent;
import io.milton.mail.Filter;
import io.milton.mail.FilterChain;
import io.milton.mail.MailResourceFactory;
import io.milton.mail.Mailbox;
import io.milton.mail.MailboxAddress;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.CommandHandler;
import org.subethamail.smtp.server.SMTPServer;

public class SubethaSmtpServer implements SimpleMessageListener, SmtpServer {
    private final static Logger log = LoggerFactory.getLogger(SubethaSmtpServer.class);

    protected SMTPServer smtpReceivingServer;
    protected final int smtpPort;
    protected final boolean enableTls;
    protected final MailResourceFactory resourceFactory;
    protected final List<Filter> filters;

    public SubethaSmtpServer(int smtpPort, boolean enableTls, MailResourceFactory resourceFactory, List<Filter> filters) {
        if( resourceFactory ==null ) throw new RuntimeException( "Configuration problem. resourceFactory cannot be null");
        this.smtpPort = smtpPort;
        this.enableTls = enableTls;
        this.resourceFactory = resourceFactory;
        this.filters = filters;
    }

    public SubethaSmtpServer(MailResourceFactory resourceFactory, List<Filter> filters) {
        this(25,false,resourceFactory, filters);
    }


    @Override
    public void start() {
        initSmtpReceiver();

        log.info("starting SMTP server on port: " + this.smtpReceivingServer.getPort() + " address: " + this.smtpReceivingServer.getBindAddress());
        try {
            this.smtpReceivingServer.start();
        } catch (Throwable e) {
            throw new RuntimeException("Exception starting SMTP server. port: " + this.smtpReceivingServer.getPort() + " address: " + this.smtpReceivingServer.getBindAddress(), e);
        }
        log.info("Geroa email server started.");
    }

    @Override
    public void stop() {
        try {
            smtpReceivingServer.stop();
        } catch( Exception e ) {
            log.debug( "exception stopping smtp receiver: " + e.getMessage()); // probably interrupted ex
        }
        smtpReceivingServer = null;
    }

    protected String getSubjectDontThrow(MimeMessage mm) {
        try {
            return mm.getSubject();
        } catch (MessagingException ex) {
            return "[couldnt_read_subject]";
        }
    }

    protected void initSmtpReceiver() {
        if( enableTls ) {
            log.info("Creating TLS enabled server");
            this.smtpReceivingServer = new SMTPServer(new SimpleMessageListenerAdapter(this));
        } else {
            log.info("Creating TLS DIS-abled server");
            this.smtpReceivingServer = new TlsDisabledSmtpServer(new SimpleMessageListenerAdapter(this));
        }
        this.smtpReceivingServer.setPort(smtpPort);
        this.smtpReceivingServer.setMaxConnections(30000);
        CommandHandler cmdHandler = this.smtpReceivingServer.getCommandHandler();

        this.smtpReceivingServer.setAuthenticationHandlerFactory(null);
    }



    /**
     * Subetha.MessageListener
     *
     */
    @Override
    public boolean accept(String sFrom, String sRecipient) {
        log.debug("accept? " + sFrom + " - " +sRecipient);
        if( sFrom == null || sFrom.length() == 0 ) {
            log.error("Cannot accept email with no from address. Recipient is: " + sRecipient);
            return false;
        }
        final AcceptEvent event = new AcceptEvent(sFrom, sRecipient);
        Filter terminal = (chain, e) -> {
            MailboxAddress recip = MailboxAddress.parse(event.getRecipient());
            Mailbox recipMailbox = resourceFactory.getMailbox(recip);

            boolean b = (recipMailbox != null && !recipMailbox.isEmailDisabled());
            log.debug("accept email from: " + event.getFrom() + " to: " + event.getRecipient() + "?" + b);
            event.setAccept(b);
        };
        FilterChain chain = new FilterChain(filters, terminal);
        chain.doEvent(event);
        return event.isAccept();
    }

    /**
     * Subetha MessageListener. Called when an SMTP message has bee received. Could
     * be a send request from our domain or an email to our domain
     *
     */
    @Override
    public void deliver(String sFrom, String sRecipient, final InputStream data) throws IOException {
        log.debug("deliver email from: " + sFrom + " to: " + sRecipient);
        log.debug("email from: " + sFrom + " to: " + sRecipient);
        final DeliverEvent event = new DeliverEvent(sFrom, sRecipient, data);
        Filter terminal = (chain, e) -> {
            MailboxAddress from = MailboxAddress.parse(event.getFrom());
            MailboxAddress recip = MailboxAddress.parse(event.getRecipient());

            MimeMessage mm = parseInput(data);

            Mailbox recipMailbox = resourceFactory.getMailbox(recip);
            log.debug("recipient is known to us, so store: " + recip);
            storeMail(recipMailbox,mm);

        };
        FilterChain chain = new FilterChain(filters, terminal);
        chain.doEvent(event);
    }

    protected MimeMessage parseInput(InputStream data) {
        try {
            MimeMessage mm = new MimeMessage( getSession(), data );
            log.debug( "encoding: " + mm.getEncoding());
            return mm;
            //return new SMTPMessage(getSession(), data);
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected  Session getSession() {
        return null;
    }


    protected void storeMail(Mailbox recipMailbox, MimeMessage mm) {
        try {
            recipMailbox.storeMail(mm);
        } catch (Throwable e) {
            String subject = getSubjectDontThrow(mm);
            log.error("Exception storing mail. mailbox: " + recipMailbox.getClass() + " message: " + subject,e);
        }
    }

    @Override
    public int getSmtpPort() {
        return smtpPort;
    }

    public boolean isEnableTls() {
        return enableTls;
    }

    public MailResourceFactory getResourceFactory() {
        return resourceFactory;
    }


}
