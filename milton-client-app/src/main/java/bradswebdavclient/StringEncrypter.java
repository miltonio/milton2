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

import com.bradmcevoy.io.Base64Coder;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;


public class StringEncrypter {
    
    public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
    public static final String DES_ENCRYPTION_SCHEME = "DES";
    public static final String			DEFAULT_ENCRYPTION_KEY	= "This is a fairly long phrase used to encrypt";
    
    private KeySpec				keySpec;
    private SecretKeyFactory	keyFactory;
    private Cipher				cipher;
    
    private static final String	UNICODE_FORMAT			= "UTF8";
    
    public static StringEncrypter getInstance() throws EncryptionException {
        return new StringEncrypter(DES_ENCRYPTION_SCHEME);
    }
    
    public StringEncrypter( String encryptionScheme ) throws EncryptionException {
        this( encryptionScheme, DEFAULT_ENCRYPTION_KEY );
    }
    
    public StringEncrypter( String encryptionScheme, String encryptionKey ) throws EncryptionException {
        
        if ( encryptionKey == null )
            throw new IllegalArgumentException( "encryption key was null" );
        if ( encryptionKey.trim().length() < 24 )
            throw new IllegalArgumentException("encryption key was less than 24 characters" );
        
        try {
            byte[] keyAsBytes = encryptionKey.getBytes( UNICODE_FORMAT );
            
            if ( encryptionScheme.equals( DESEDE_ENCRYPTION_SCHEME) ) {
                keySpec = new DESedeKeySpec( keyAsBytes );
            } else if ( encryptionScheme.equals( DES_ENCRYPTION_SCHEME ) ) {
                keySpec = new DESKeySpec( keyAsBytes );
            } else {
                throw new IllegalArgumentException( "Encryption scheme not supported: " + encryptionScheme );
            }
            
            keyFactory = SecretKeyFactory.getInstance( encryptionScheme );
            cipher = Cipher.getInstance( encryptionScheme );
            
        } catch (InvalidKeyException e) {
            throw new EncryptionException( e );
        } catch (UnsupportedEncodingException e) {
            throw new EncryptionException( e );
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException( e );
        } catch (NoSuchPaddingException e) {
            throw new EncryptionException( e );
        }
        
    }
    
    public String encrypt( String unencryptedString ) throws EncryptionException {
        if ( unencryptedString == null || unencryptedString.trim().length() == 0 ) throw new IllegalArgumentException("unencrypted string was null or empty" );
        
        try {
            SecretKey key = keyFactory.generateSecret( keySpec );
            cipher.init( Cipher.ENCRYPT_MODE, key );
            byte[] cleartext = unencryptedString.getBytes( UNICODE_FORMAT );
            byte[] ciphertext = cipher.doFinal( cleartext );
            
            return encodeBase64( ciphertext );
        } catch (Exception e) {
            throw new EncryptionException( e );
        }
    }
    
    public String decrypt( String encryptedString ) throws EncryptionException {
        if ( encryptedString == null || encryptedString.trim().length() <= 0 )
            throw new IllegalArgumentException( "encrypted string was null or empty" );
        
        try {
            SecretKey key = keyFactory.generateSecret( keySpec );
            cipher.init( Cipher.DECRYPT_MODE, key );
            byte[] cleartext = decodeBase64( encryptedString );
            byte[] ciphertext = cipher.doFinal( cleartext );
            
            return bytes2String( ciphertext );
        } catch (Exception e) {
            throw new EncryptionException( e );
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
    
    private static String bytes2String( byte[] bytes ) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            stringBuffer.append( (char) bytes[i] );
        }
        return stringBuffer.toString();
    }
    
    public static class EncryptionException extends Exception {
        private static final long serialVersionUID = 1L;
        public EncryptionException( Throwable t ) {
            super( t );
        }
    }
}