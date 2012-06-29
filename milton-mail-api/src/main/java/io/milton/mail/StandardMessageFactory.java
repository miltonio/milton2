package io.milton.mail;

import javax.mail.internet.MimeMessage;

/**
 *  A service to copy information from a MimeMessage to a
 *  StandardMessage and vice versa
 */
public interface  StandardMessageFactory {

    /**
     * copy information from the given standard messsage into a new MimeMessage
     * object, which will be constructed on the given Session
     *
     * @param sm
     * @param mm - 
     */
    void toMimeMessage(StandardMessage sm, MimeMessage mm);

    /**
     * Copy information from the MimeMessage into the standard message
     *
     * @param mm - the MimeMessage which is the source of information
     * @param sm - the standard message to be populated    
     */
    void toStandardMessage(MimeMessage mm, StandardMessage sm);
}
