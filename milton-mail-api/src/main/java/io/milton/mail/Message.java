package io.milton.mail;

/**
 *
 * @author brad
 */
public class  Message {

    final MessageResource resource;
    final int id;

    private boolean deleted;

    public Message(MessageResource resource, int id) {
        this.resource = resource;
        this.id = id;
    }

    public void markForDeletion() {
        deleted = true;
    }

    public boolean isMarkedForDeletion() {
        return deleted;
    }

    public int getId() {
        return id;
    }

    public int size() {
        return resource.getSize();
    }

    public MessageResource getResource() {
        return resource;
    }

    
}
