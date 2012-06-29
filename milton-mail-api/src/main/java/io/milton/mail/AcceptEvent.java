package io.milton.mail;

/**
 *
 */
public class AcceptEvent extends AbstractReceiveEvent {
    private boolean accept;

    public AcceptEvent(String sFrom, String sRecipient) {
        super(sFrom, sRecipient);
    }

    /**
     * Set is the email for this event should be accepted
     *
     * @param accept
     */
    public void setAccept(boolean accept) {
        this.accept = accept;
    }

    public boolean isAccept() {
        return accept;
    }

}
