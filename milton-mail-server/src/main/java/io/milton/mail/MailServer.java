package io.milton.mail;

import io.milton.mail.pop.PopServer;
import io.milton.mail.receive.SmtpServer;
import io.milton.mail.send.MailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailServer {

    private final static Logger log = LoggerFactory.getLogger( MailServer.class );
    private MailSender mailSender;
    private SmtpServer smtpServer;
    private SmtpServer msaSmtpServer;
    private PopServer popServer;


    /**
     * Create the mail server witht the given components. Any of these may be null,
     * in which case that service will be unavailable
     *
     * @param mailSender
     * @param smtpServer - for inter-server transmission
     * @param popServer
     * @param msaSmtpServer - MSA agent, for mail submission from end users
     */
    public MailServer( MailSender mailSender, SmtpServer smtpServer, PopServer popServer, SmtpServer msaSmtpServer ) {
        this.mailSender = mailSender;
        this.smtpServer = smtpServer;
        this.popServer = popServer;
        this.msaSmtpServer = msaSmtpServer;
    }

    public void start() {
        log.debug( "starting mail servers..." );
        if( mailSender != null ) {
            log.debug( "starting mail sender.." );
            mailSender.start();
        }
        if( smtpServer != null ) {
            log.debug( "starting smtp receiver.." );
            smtpServer.start();
        }
        if( msaSmtpServer != null ) {
            log.debug( "starting authenticated smtp server.." );
            if( smtpServer != null && smtpServer.getSmtpPort() == msaSmtpServer.getSmtpPort() ) {
                log.error( "The Authenticated SMTP server is configured to run on the same port as the un-authenticated server: " + msaSmtpServer.getSmtpPort() + ". The authenticated one should be on 587 and non-auth on 25. See - http://www.ietf.org/rfc/rfc2476.txt" );
            } else {
                msaSmtpServer.start();
            }
        }
        if( popServer != null ) {
            log.debug( "starting pop server.." );
            popServer.start();
        }
        log.debug( "...done loading mail servers" );
    }

    public void stop() {
        log.debug( "stopping mail server components..." );
        if( mailSender != null ) {
            try {
                mailSender.stop();
            } catch( Throwable e ) {
                log.debug( "exception stopping mailsender: " + e.getMessage());
            }
        }
        if( smtpServer != null ) {
            try {
                smtpServer.stop();
            } catch( Throwable e ) {
                log.debug( "exception stopping smtp server: " + e.getMessage());
            }
        }
        if( msaSmtpServer != null ) {
            try {
                msaSmtpServer.stop();
            } catch( Throwable e ) {
                log.debug( "exception stopping msa server: " + e.getMessage());
            }
        }
        if( popServer != null ) {
            try {
                popServer.stop();
            } catch( Throwable e ) {
                log.debug( "exception stopping pop server: " + e.getMessage());
            }
        }
        log.debug( "...done stopping mail servers" );
    }

    public MailSender getMailSender() {
        return mailSender;
    }

    public PopServer getPopServer() {
        return popServer;
    }

    public SmtpServer getSmtpServer() {
        return smtpServer;
    }

    public void setMailSender( MailSender mailSender ) {
        this.mailSender = mailSender;
    }

    public void setPopServer( PopServer popServer ) {
        this.popServer = popServer;
    }

    public void setSmtpServer( SmtpServer smtpServer ) {
        this.smtpServer = smtpServer;
    }
}
