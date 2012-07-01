package io.milton.mail;

import javax.mail.internet.MimeMessage;

/**
 *
 * @author brad
 */
public interface Mailbox {

    /**
     * Required for SMTP and pop command in POP
     * 
     * @param password
     * @return 
     */
    boolean authenticate(String password);
    
    /**
     * Required for apop command in POP protocol
     * 
     * @param passwordHash
     * @return 
     */
    boolean authenticateMD5(byte[] passwordHash);

    /**
     * Locate the primary folder to deliver mail to
     * 
     * @return 
     */
    MessageFolder getInbox();
    
    /**
     * Locate the named folder
     * 
     * @param name
     * @return 
     */
    MessageFolder getMailFolder(String name);

    /**
     * 
     * @return - true iff this mailbox is enabled for sending and receiving emails
     */
    boolean isEmailDisabled();
    
    /**
     * Called when a message has been received for delivery to this user. The message
     * should be stored in the user's inbox
     * 
     * @param mm 
     */
    void storeMail(MimeMessage mm);

}
