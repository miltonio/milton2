package io.milton.mail.memory;

import io.milton.mail.Mailbox;
import io.milton.mail.MessageFolder;
import io.milton.mail.StandardMessageFactory;
import io.milton.mail.StandardMessageFactoryImpl;
import java.util.HashMap;
import java.util.Map;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class MemoryMailBox implements Mailbox{

    private final static Logger log = LoggerFactory.getLogger(MemoryMailBox.class);

    private static final StandardMessageFactory factory = new StandardMessageFactoryImpl();

    String password;
    Map<String,MessageFolder> folders;

    public MemoryMailBox() {
        folders = new HashMap<String, MessageFolder>();
        MemoryMessageFolder folder = addFolder("inbox");
        this.password = "password";
    }

    public boolean authenticate(String password) {
        return password.equals(this.password);
    }

    public boolean authenticateMD5(byte[] passwordHash) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageFolder getInbox() {
        return folders.get("inbox");
    }

    public MessageFolder getMailFolder(String name) {
        return folders.get(name);
    }

    public boolean isEmailDisabled() {
        return false;
    }

    public void storeMail(MimeMessage mm) {
        MemoryMessageFolder folder = (MemoryMessageFolder) getInbox();
        MemoryMessageResource res = new MemoryMessageResource(folder, mm, factory);
        folder.messages.add(res);
    }

    public MemoryMessageFolder addFolder(String name) {
        MemoryMessageFolder folder = new MemoryMessageFolder();
        folders.put(name,folder);
        return folder;
    }
}
