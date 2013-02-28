package io.milton.mail.receive;

/**
 *
 * @author brad
 */
public interface SmtpServer {

    void start();

    void stop();

    int getSmtpPort();
}
