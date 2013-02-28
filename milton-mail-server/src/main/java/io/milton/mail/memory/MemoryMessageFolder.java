package io.milton.mail.memory;

import io.milton.mail.MessageFolder;
import io.milton.mail.MessageResource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryMessageFolder implements MessageFolder {

    private final static Logger log = LoggerFactory.getLogger(MemoryMessageFolder.class);

    List<MessageResource> messages = new ArrayList<MessageResource>();

    public Collection<MessageResource> getMessages() {
        return messages;
    }

    public int numMessages() {
        return messages.size();
    }

    public int totalSize() {
        int size = 0;
        for( MessageResource res : messages ) {
            size += res.getSize();
        }
        log.debug( "total size: " + size );
        return size;
    }
}
