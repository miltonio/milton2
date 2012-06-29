package io.milton.mail;

import java.io.InputStream;

/**
 *  Represents an attachment to an email, including metadata
 */
public interface Attachment {
    /**
     *
     * @return - the name of this item within its message
     */
    String getName();

    /**
     * Use the data of the attachment. The implementation will open an input
     * stream, provide it to the given closure/callback, and the close it and
     * release any resources after the method has completed
     *
     * Do not close the stream
     *
     * @param exec - closure which will consume the stream of data
     */
    void useData(InputStreamConsumer exec);

    /**
     * This method is necessary for DataSource support, but useData is preferred
     * 
     * @return - an inputstream to read from.
     */
    InputStream getInputStream();

    /**
     *
     * @return - the size in bytes of the attachment
     */
    int size();

    /**
     *
     * @return - the complete content id header Eg <abc123>
     */
    String getContentId();

    /**
     *
     * @return - the content type header eg text/plain
     */
    String getContentType();

    /**
     *
     * @return - the disposition for this attachment in a mime message. Eg Part.ATTACHMENT
     */
    String getDisposition();
}
