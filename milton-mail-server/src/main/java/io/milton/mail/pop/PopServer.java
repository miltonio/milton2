package io.milton.mail.pop;

/**
 *
 * @author brad
 */
public interface PopServer {
    void start();
    void stop();

    int getPopPort();
}
