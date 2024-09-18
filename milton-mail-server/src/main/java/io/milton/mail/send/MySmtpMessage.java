
package io.milton.mail.send;

import com.sun.mail.smtp.SMTPMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

public class MySmtpMessage extends SMTPMessage {
        public MySmtpMessage(Session session, MimeMessage mm) throws MessagingException {
            super(mm);
            this.session = session;
        }

}
