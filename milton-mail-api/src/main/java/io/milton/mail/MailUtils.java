package io.milton.mail;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class MailUtils {
    public static byte[] md5Digest(String s) throws NoSuchAlgorithmException {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            byte[] actual = digest.digest(s.getBytes("UTF-8"));
            return actual;
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
}
