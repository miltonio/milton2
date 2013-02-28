
package io.milton.mail.send;

import com.sun.mail.smtp.SMTPMessage;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class MySmtpMessage extends SMTPMessage {
        public MySmtpMessage(Session session, MimeMessage mm) throws MessagingException {
            super(mm);
            this.session = session;
        }

}
