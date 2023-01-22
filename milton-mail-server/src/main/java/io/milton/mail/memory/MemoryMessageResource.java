package io.milton.mail.memory;

import io.milton.mail.MessageResource;
import io.milton.mail.StandardMessage;
import io.milton.mail.StandardMessageFactory;
import io.milton.mail.StandardMessageImpl;
import java.io.IOException;
import java.io.OutputStream;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryMessageResource implements MessageResource {

    private final static Logger log = LoggerFactory.getLogger(MemoryMessageResource.class);

    private final StandardMessageFactory factory;

    final MemoryMessageFolder folder;
    final StandardMessage message;

    public MemoryMessageResource( MemoryMessageFolder folder, MimeMessage mimeMessage, StandardMessageFactory factory ) {
        super();
        this.factory = factory;
        this.folder = folder;
        this.message = new StandardMessageImpl();
        factory.toStandardMessage( mimeMessage, this.message );
    }

    public void deleteMessage() {
        folder.messages.remove( this );
    }

    public int size() {
        return message.getSize();
    }

    public void writeTo( OutputStream out ) {
        MimeMessage mm = new MimeMessage( (Session) null );
        factory.toMimeMessage( message, mm );
        try {
            mm.writeTo( out );
        } catch( IOException | MessagingException ex ) {
            throw new RuntimeException( ex );
        }
    }

    public int getSize() {
        return message.getSize();
    }
}
