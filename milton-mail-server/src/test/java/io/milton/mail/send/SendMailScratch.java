package io.milton.mail.send;

import io.milton.mail.MailboxAddress;
import io.milton.mail.StandardMessageImpl;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class SendMailScratch {
    public static void main(String[] arr) {
        
        String host = "XXX";
        String user = "XXX";
        String password = "XXX";
        int port = 465;
        
        RemoteSmtpMailSender sender = new RemoteSmtpMailSender(host, user, password, port);
        sender.setUseSsl(true);
        sender.start();
        
        StandardMessageImpl sm = new StandardMessageImpl();
        sm.setFrom(MailboxAddress.parse("brad@bradmcevoy.com"));
        List<MailboxAddress> to = new ArrayList();
        to.add(MailboxAddress.parse("brad@bradmcevoy.com"));
        sm.setTo(to);
        sm.setSubject("hello!");
        
        sender.sendMail(sm);
    }
}
