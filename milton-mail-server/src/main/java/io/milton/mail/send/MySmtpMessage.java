
package io.milton.mail.send;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.eclipse.angus.mail.smtp.SMTPMessage;

public class MySmtpMessage extends SMTPMessage {
        public MySmtpMessage(Session session, MimeMessage mm) throws MessagingException {
            super(mm);
            this.session = session;
        }

}
