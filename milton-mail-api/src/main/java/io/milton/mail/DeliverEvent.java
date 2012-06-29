package io.milton.mail;

import java.io.InputStream;

/**
 *
 */
public class DeliverEvent extends AbstractReceiveEvent {
    final InputStream inputStream;

    public DeliverEvent(String sFrom, String sRecipient, InputStream inputStream) {
        super(sFrom, sRecipient);
        this.inputStream = inputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    
}
