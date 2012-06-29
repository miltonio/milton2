package io.milton.mail;

import java.util.Collection;

/**
 *
 */
public interface HtmlMessageResource extends MessageResource {
    String getHtmlContent();
    public Collection<Attachment> getAttachments();
    
}
