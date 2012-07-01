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
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.masukomi.aspirin.core.AspirinInternal;
import org.masukomi.aspirin.core.config.Configuration;
import org.masukomi.aspirin.core.delivery.DeliveryManager;
import org.masukomi.aspirin.core.listener.AspirinListener;
import org.masukomi.aspirin.core.listener.ListenerManager;
import org.masukomi.aspirin.core.listener.ResultState;
import org.masukomi.aspirin.core.store.mail.FileMailStore;
import org.masukomi.aspirin.core.store.mail.MailStore;
import org.masukomi.aspirin.core.store.queue.QueueStore;
import org.masukomi.aspirin.core.store.queue.SimpleQueueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AspirinMailSender implements MailSender, AspirinListener {

    private final static Logger log = LoggerFactory.getLogger(AspirinMailSender.class);
    private final AspirinInternal aspirin;
    private final ListenerManager listenerManager;
    private final DeliveryManager deliveryManager;
  
    private boolean started;

 


    /**
     * 
     * @param retryInterval - eg 1000
     * @param deliveryThreads - eg 2
     * @param postmaster - eg admin@ettrema.com
     * @param maxRetries - eg 3
     */
    public AspirinMailSender(QueueStore queueStore, MailStore mailStore) {
        listenerManager = new ListenerManager();
        Configuration configuration = new Configuration();
        deliveryManager = new DeliveryManager(configuration, queueStore, mailStore);
        listenerManager.setDeliveryManager(deliveryManager);
        aspirin = new AspirinInternal(configuration, deliveryManager, listenerManager);
    }

    @Override
    public void sendMail(MimeMessage mm) {
        if( !started ) {
            throw new RuntimeException("This mail sender is stopped");
        }
        try {
            aspirin.add(mm, -1);
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }



    @Override
    public void sendMail(String from, String fromPersonal, List<String> to, String replyTo, String subject, String text) {
        if( !started ) {
            throw new RuntimeException("This mail sender is stopped");
        }
        try {
            MimeMessage mm = new MimeMessage(getSession());
            mm.setSubject(subject);
            mm.setFrom(new InternetAddress(from, fromPersonal));
            Address[] add = new Address[1];
            add[0] = new InternetAddress(replyTo);
            mm.setReplyTo(add);
            for (String sTo : to) {
                mm.addRecipient(RecipientType.TO, new InternetAddress(sTo));
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
    public Session getSession() {
        Properties props = new Properties();        
        return Session.getInstance(props);
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
        this.started = true;
        listenerManager.add(this);
    }

    @Override
    public void stop() {
        this.started = false;
        listenerManager.remove(this);
        deliveryManager.shutdown();
    }

    @Override
    public void sendMail( StandardMessage sm ) {
        StandardMessageFactory smf = new StandardMessageFactoryImpl();
        MimeMessage mm  = newMessage();
        smf.toMimeMessage( sm, mm );
        sendMail( mm );
    }

    @Override
    public void delivered(String mailId, String recipient, ResultState state, String resultContent) {
        log.info("delivered: " + recipient + " state: " + state + " result: " + resultContent);
    }
}
