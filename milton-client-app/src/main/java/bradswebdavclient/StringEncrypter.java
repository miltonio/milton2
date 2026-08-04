/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package bradswebdavclient;

import org.base64coder.Base64Coder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class StringEncrypter {

    public static final String AES_ENCRYPTION_SCHEME = "AES/GCM/NoPadding";
    public static final String DEFAULT_ENCRYPTION_KEY = "This is a fairly long phrase used to encrypt";

    private SecretKeySpec secretKey;
    private Cipher cipher;

    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_LENGTH_BYTES = 12;

    public static StringEncrypter getInstance() throws EncryptionException {
        return new StringEncrypter(AES_ENCRYPTION_SCHEME);
    }

    public StringEncrypter(String encryptionScheme) throws EncryptionException {
        this(encryptionScheme, DEFAULT_ENCRYPTION_KEY);
    }

    public StringEncrypter(String encryptionScheme, String encryptionKey) throws EncryptionException {

        if (encryptionKey == null)
            throw new IllegalArgumentException("encryption key was null");
        if (encryptionKey.trim().length() < 24)
            throw new IllegalArgumentException("encryption key was less than 24 characters");
        if (!AES_ENCRYPTION_SCHEME.equals(encryptionScheme))
            throw new IllegalArgumentException("Encryption scheme not supported: " + encryptionScheme);

        try {
            byte[] keyMaterial = MessageDigest.getInstance("SHA-256").digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
            this.secretKey = new SecretKeySpec(Arrays.copyOf(keyMaterial, 16), "AES");
            this.cipher = Cipher.getInstance(AES_ENCRYPTION_SCHEME);
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException(e);
        } catch (GeneralSecurityException e) {
            throw new EncryptionException(e);
        }

    }

    public String encrypt(String unencryptedString) throws EncryptionException {
        if (unencryptedString == null || unencryptedString.trim().length() == 0)
            throw new IllegalArgumentException("unencrypted string was null or empty");

        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            byte[] cleartext = unencryptedString.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(cleartext);
            byte[] out = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ciphertext, 0, out, iv.length, ciphertext.length);

            return encodeBase64(out);
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    public String decrypt(String encryptedString) throws EncryptionException {
        if (encryptedString == null || encryptedString.trim().length() <= 0)
            throw new IllegalArgumentException("encrypted string was null or empty");

        try {
            byte[] input = decodeBase64(encryptedString);
            if (input.length <= GCM_IV_LENGTH_BYTES) {
                throw new IllegalArgumentException("encrypted payload is invalid");
            }
            byte[] iv = Arrays.copyOfRange(input, 0, GCM_IV_LENGTH_BYTES);
            byte[] ciphertext = Arrays.copyOfRange(input, GCM_IV_LENGTH_BYTES, input.length);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            byte[] cleartext = cipher.doFinal(ciphertext);

            return new String(cleartext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    public static String encodeBase64(byte[] arr) {
        char[] chars = Base64Coder.encode(arr);
        return String.valueOf(chars);
    }

    public static byte[] decodeBase64(String s) throws IOException {
        char[] chars = s.toCharArray();
        return Base64Coder.decode(chars);
    }


    public static class EncryptionException extends Exception {
        private static final long serialVersionUID = 1L;

        public EncryptionException(Throwable t) {
            super(t);
        }
    }
}
