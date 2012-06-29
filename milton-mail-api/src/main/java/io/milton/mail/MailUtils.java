package io.milton.mail;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 */
public class MailUtils {
    public static byte[] md5Digest(String s) throws NoSuchAlgorithmException {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            byte[] actual = digest.digest(s.getBytes());
            return actual;
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
}
