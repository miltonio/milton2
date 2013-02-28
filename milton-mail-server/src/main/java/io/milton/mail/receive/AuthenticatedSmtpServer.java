
package io.milton.mail.receive;

import io.milton.mail.AcceptEvent;
import io.milton.mail.DeliverEvent;
import io.milton.mail.Event;
import io.milton.mail.Filter;
import io.milton.mail.FilterChain;
import io.milton.mail.LoginEvent;
import io.milton.mail.send.MailSender;
import io.milton.mail.MailResourceFactory;
import io.milton.mail.Mailbox;
import io.milton.mail.MailboxAddress;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.auth.LoginAuthenticationHandler;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.PlainAuthenticationHandler;
import org.subethamail.smtp.auth.PluginAuthenticationHandler;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.server.MessageListenerAdapter;

/**
 * A SMTP server for end-users to send mail to. This is often on port 25, but this
 * will conflict with the SMTP server for inter-server communications if one
 * is running on the same host
 *
 * According to the spec, this type of service should run on port 587. See
 * http://www.ietf.org/rfc/rfc2476.txt
 *
 *
 * 3.  Message Submission

3.1.  Submission Identification

   Port 587 is reserved for email message submission as specified in
   this document.  Messages received on this port are defined to be
   submissions.  The protocol used is ESMTP [SMTP-MTA, ESMTP], with
   additional restrictions as specified here.

   While most email clients and servers can be configured to use port
   587 instead of 25, there are cases where this is not possible or
   convenient.  A site MAY choose to use port 25 for message submission,
   by designating some hosts to be MSAs and others to be MTAs.

 *
 * @author brad
 */
public class AuthenticatedSmtpServer extends SubethaSmtpServer {
    private final static Logger log = LoggerFactory.getLogger(AuthenticatedSmtpServer.class);

    private final MailSender mailSender;
    
    public AuthenticatedSmtpServer(int smtpPort, boolean enableTls, MailResourceFactory resourceFactory, MailSender mailSender, List<Filter> filters) {
        super(smtpPort, enableTls, resourceFactory, filters);
        this.mailSender = mailSender;
    }

    public AuthenticatedSmtpServer(MailResourceFactory resourceFactory, MailSender mailSender, List<Filter> filters) {
        super(587, false, resourceFactory, filters);
        this.mailSender = mailSender;
    }

    
    
    @Override
    protected void initSmtpReceiver() {
        super.initSmtpReceiver();
        MessageListenerAdapter mla = (MessageListenerAdapter) smtpReceivingServer.getMessageHandlerFactory();
        mla.setAuthenticationHandlerFactory(new AuthHandlerFactory());
    }
    
    
    /**
     * Sends the message assuming that this mimemessage was constructed on the MailSender's
     * session
     * 
     * @param mm
     */
    public void sendMail(MimeMessage mm) {
        mailSender.sendMail(mm);
    }
    
    public void sendMail(String fromAddress, String fromPersonal,List<String> to, String replyTo, String subject, String text) {
        mailSender.sendMail(fromAddress, fromPersonal, to, replyTo, subject, text);
    }
    
    /**
     *
     * @return - the session used by the mail sender. can be used to build smtpmessage objects
     */
    public Session getSmtpSendSession() {
        return mailSender.getSession();
    }

            
    /**
     * Subetha.MessageListener
     * 
     * Always accept everything when receiving SMTP messages
     */
    @Override
    public boolean accept(String sFrom, String sRecipient) {
        log.debug("accept? " + sFrom + " - " +sRecipient);
        if( sFrom == null || sFrom.length() == 0 ) {
            log.error("Cannot accept email with no from address. Recipient is: " + sRecipient);
            return false;
        }
        final AcceptEvent event = new AcceptEvent(sFrom, sRecipient);
        Filter terminal = new Filter() {

            public void doEvent(FilterChain chain, Event e) {
                MailboxAddress from = MailboxAddress.parse(event.getFrom());
                Mailbox fromMailbox = resourceFactory.getMailbox(from);
                if (fromMailbox != null && !fromMailbox.isEmailDisabled() ) {
                    event.setAccept(true);
                    return ;
                }
                MailboxAddress recip = MailboxAddress.parse(event.getRecipient());
                Mailbox recipMailbox = resourceFactory.getMailbox(recip);

                boolean b = (recipMailbox != null && !recipMailbox.isEmailDisabled());
                log.debug("accept email from: " + event.getFrom() + " to: " + event.getRecipient() + "?" + b);
                event.setAccept(b);
            }
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
    public void deliver(String sFrom, String sRecipient, final InputStream data) throws TooMuchDataException, IOException {
        log.debug("deliver email from: " + sFrom + " to: " + sRecipient);
        log.debug("email from: " + sFrom + " to: " + sRecipient);
        final DeliverEvent event = new DeliverEvent(sFrom, sRecipient, data);
        Filter terminal = new Filter() {

            public void doEvent(FilterChain chain, Event e) {
                MailboxAddress from = MailboxAddress.parse(event.getFrom());
                MailboxAddress recip = MailboxAddress.parse(event.getRecipient());

                MimeMessage mm = parseInput(data);

                Mailbox recipMailbox = resourceFactory.getMailbox(recip);
                if (recipMailbox != null && !recipMailbox.isEmailDisabled()) {
                    log.debug("recipient is known to us, so store: " + recip);
                    storeMail(recipMailbox,mm);
                } else {
                    Mailbox fromMailbox = resourceFactory.getMailbox(from);
                    if (fromMailbox != null && !fromMailbox.isEmailDisabled() ) {
                        log.debug("known from address, so will transmit: from: " + from);
                        mailSender.sendMail(mm);
                    } else {
                        throw new NullPointerException("Neither from address nor recipient are known to us. Will not store or send: from: " + event.getFrom() + " to: " + event.getRecipient());
                    }
                }

            }
        };
        FilterChain chain = new FilterChain(filters, terminal);
        chain.doEvent(event);
    }

    
    /**
     * Creates the JavaMail Session object for use in WiserMessage
     */
    @Override
    protected Session getSession() {
        return mailSender.getSession();
    }

    /**
     * Creates the AuthHandlerFactory which logs the user/pass.
     */
    public class AuthHandlerFactory implements AuthenticationHandlerFactory {

        public AuthenticationHandler create() {
            PluginAuthenticationHandler ret = new PluginAuthenticationHandler();
            UsernamePasswordValidator validator = new UsernamePasswordValidator() {

                public void login(String username, String password) throws LoginFailedException {
                    boolean loginOk = doLogin(username, password);
                    if (!loginOk) {
                        throw new LoginFailedException("authentication failed");
                    }

                }
            };
            ret.addPlugin(new PlainAuthenticationHandler(validator));
            ret.addPlugin(new LoginAuthenticationHandler(validator));
            return ret;
        }
    }

    public boolean doLogin(String username, String password) {
        final LoginEvent event = new LoginEvent(username, password);
        Filter terminal = new Filter() {

            public void doEvent(FilterChain chain, Event e) {
                event.setLoginSuccessful( _doLogin(event.getUsername(), event.getPassword()) );
            }
        };
        FilterChain chain = new FilterChain(filters, terminal);
        chain.doEvent(event);
        return event.isLoginSuccessful();
    }

    public boolean _doLogin(String username, String password) {
        try {
            MailboxAddress userName = MailboxAddress.parse(username);
            Mailbox mbox = resourceFactory.getMailbox(userName);
            if (mbox == null) {
                log.debug("user not found");
                return false;
            }
            if (!mbox.authenticate(password)) {
                log.debug("authentication failed");
                return false;
            }
            return true;
        } catch (IllegalArgumentException ex) {
            log.debug("username could not be parsed. use form user@domain.com");
            return false;
        }

    }

    public MailSender getMailSender() {
        return mailSender;
    }
}
