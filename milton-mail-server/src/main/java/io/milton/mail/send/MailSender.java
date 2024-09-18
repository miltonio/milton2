package io.milton.mail.send;

import io.milton.mail.StandardMessage;
import java.util.List;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

/**
 * Implements high level abstraction on sending emails. The actual message
 * transmission might be done via smtp relay, direct smtp transmission to
 * recipient domains, etc
 *
 * Sending might be synchronous or asynchronous, so successful completion of
 * a call should not be taken to assume successful delivery
 *
 * @author brad
 */
public interface MailSender {

    void start();

    void stop();

    /**
     *
     * @param fromAddress
     * @param fromPersonal
     * @param to
     * @param replyTo
     * @param subject
     * @param text
     */
    void sendMail(String fromAddress, String fromPersonal, List<String> to, String replyTo, String subject, String text);

    /**
     *
     * @return - a mail session object suitable for constructing and sending messages
     */
    Session getSession();

    /**
     * Sends a message, assuming it was constructed using this MailSender's getSession
     *
     * @param mm
     */
    void sendMail(MimeMessage mm);

    /**
     * Sends the given standard message. This will usually be firt converted
     * to a MimeMessage by the default StandardMessageFactory implementation
     *
     * @param mm
     */
    void sendMail(StandardMessage sm);

    /**
     * create a new message which is a logical clone of the one given
     *
     * @param mm
     * @return
     */
    MimeMessage newMessage(MimeMessage mm);

    /**
     * create a new mimemessage on the current session
     *
     * @return
     */
    MimeMessage newMessage();
}
