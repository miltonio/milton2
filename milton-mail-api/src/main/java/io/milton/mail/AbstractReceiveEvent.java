package io.milton.mail;

/**
 *
 */
public abstract class AbstractReceiveEvent implements Event {
    final String sFrom;
    final String sRecipient;

    public AbstractReceiveEvent(String sFrom, String sRecipient) {
        this.sFrom = sFrom;
        this.sRecipient = sRecipient;
    }

    public String getFrom() {
        return sFrom;
    }

    public String getRecipient() {
        return sRecipient;
    }



    
}
