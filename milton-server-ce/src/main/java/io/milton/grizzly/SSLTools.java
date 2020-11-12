/*
 *       Copyright FuseLMS
 */
package io.milton.grizzly;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Base64;

/**
 *
 * @author dylan
 */
public class SSLTools {

	public static Certificate parseCertificate(String certificateText) throws CertificateException {
		return parseCertificate(certificateText.getBytes());
	}

	/**
	 *
	 * @param certificateBytes
	 * @return Certificate
	 * @throws CertificateException
	 */
	public static Certificate parseCertificate(byte[] certificateBytes) throws CertificateException {
		if (certificateBytes.length == 0) {
			throw new RuntimeException("Empty certificate");
		}
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		ByteArrayInputStream bais = new ByteArrayInputStream(certificateBytes);
		try {
			return cf.generateCertificate(bais);
		} catch (CertificateException certificateException) {
			throw new CertificateException("Could not read certificate", certificateException);
		}
	}

	/**
	 *
	 * @param certificateText
	 * @return
	 * @throws java.security.cert.CertificateException
	 */
	public static X509Certificate parseX509Certificate(String certificateText) throws CertificateException {
		return (X509Certificate) parseCertificate(certificateText);
	}

	/**
	 *
	 * @param certificateBytes
	 * @return
	 * @throws java.security.cert.CertificateException
	 */
	public static X509Certificate parseX509Certificate(byte[] certificateBytes) throws CertificateException {
		return (X509Certificate) parseCertificate(certificateBytes);
	}

	/**
	 *
	 * @param privateKeyText
	 * @return
	 * @throws java.security.GeneralSecurityException
	 * @throws java.io.IOException
	 */
	public static PrivateKey parsePrivateKey(final String privateKeyText) throws GeneralSecurityException, IOException {
		return parsePrivateKey(privateKeyText, null);
	}

	/**
	 *
	 * @param privateKeyText
	 * @param password
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public static PrivateKey parsePrivateKey(final String privateKeyText, final char[] password) throws GeneralSecurityException, IOException {
		return parsePrivateKey(privateKeyText.getBytes(), password);
	}

	/**
	 *
	 * @param privateKeyBytes
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public static PrivateKey parsePrivateKey(final byte[] privateKeyBytes) throws GeneralSecurityException, IOException {
		return parsePrivateKey(privateKeyBytes, null);
	}

	/**
	 *
	 * @param privateKeyBytes
	 * @param password
	 * @return
	 * @throws java.security.GeneralSecurityException
	 * @throws java.io.IOException
	 */
	public static PrivateKey parsePrivateKey(final byte[] privateKeyBytes, final char[] password) throws GeneralSecurityException, IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(privateKeyBytes);
		InputStreamReader reader = new InputStreamReader(bais);
		PEMReader parser = null;
		try {
			if (password != null) {
				parser = new PEMReader(reader, getPasswordFinder(password));
			} else {
				parser = new PEMReader(reader);
			}
			KeyPair caKeyPair = (KeyPair) parser.readObject();
			if (caKeyPair == null) {
				throw new GeneralSecurityException("Reading CA private key failed");
			}
			return caKeyPair.getPrivate();
		} finally {
			if (parser != null) {
				parser.close();
			}
			bais.close();
			reader.close();
		}

	}

	/**
	 *
	 * @param privateKeyText
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public static KeyPair parseKeyPair(final String privateKeyText) throws GeneralSecurityException, IOException {
		return parseKeyPair(privateKeyText.getBytes(), null);
	}

	/**
	 *
	 * @param privateKeyText
	 * @param password
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public static KeyPair parseKeyPair(final String privateKeyText, final String password) throws GeneralSecurityException, IOException {
		return parseKeyPair(privateKeyText.getBytes(), password);
	}

	/**
	 *
	 * @param privateKeyBytes
	 * @param password
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public static KeyPair parseKeyPair(final byte[] privateKeyBytes, final String password) throws GeneralSecurityException, IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(privateKeyBytes);
		InputStreamReader reader = new InputStreamReader(bais);
		PEMReader parser = null;
		try {
			if (password != null) {
				parser = new PEMReader(reader, getPasswordFinder(password));
			} else {
				parser = new PEMReader(reader);
			}
			KeyPair caKeyPair = (KeyPair) parser.readObject();
			if (caKeyPair == null) {
				throw new GeneralSecurityException("Reading CA private key failed");
			}
			return caKeyPair;
		} finally {
			if (parser != null) {
				parser.close();
			}
			bais.close();
			reader.close();
		}
	}

	/**
	 *
	 * @param privateKeyBytes
	 * @param password
	 * @return
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static PublicKey getPublicKeyFromPrivateKey(final byte[] privateKeyBytes, final String password) throws IOException, GeneralSecurityException {
		KeyPair keypair = parseKeyPair(privateKeyBytes, password);
		return keypair.getPublic();
	}

	/**
	 *
	 * @param privateKeyText
	 * @param password
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public static RSAPrivateKey parseRSAPrivateKey(final String privateKeyText, final char[] password) throws GeneralSecurityException, IOException {
		return (RSAPrivateKey) parsePrivateKey(privateKeyText, password);
	}

	/**
	 *
	 * @param privateKeyText
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public static RSAPrivateKey parseRSAPrivateKey(final String privateKeyText) throws GeneralSecurityException, IOException {
		return (RSAPrivateKey) parsePrivateKey(privateKeyText, null);
	}

	public static PublicKey parsePublicKey(final String publicKeyPEM) throws IOException {
		return (PublicKey) pemReader(publicKeyPEM);
	}

	/**
	 * Gets the modulus byte array from the X.509 certificate and calculates the
	 * SHA-1 digest and return a hex string representation of the SHA-1.
	 *
	 * @param cert
	 * @return String
	 * @throws java.security.NoSuchAlgorithmException
	 */
	public static String getCertificateModulusSHA1(final X509Certificate cert) throws NoSuchAlgorithmException {
		PublicKey publicKey = cert.getPublicKey();
		if (!(publicKey instanceof RSAPublicKey)) {
			throw new IllegalArgumentException("Certificate file does not contain an RSA public key but a " + publicKey.getClass().getName());
		}
		RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
		byte[] certModulusData = rsaPublicKey.getModulus().toByteArray();
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] certID = sha1.digest(certModulusData);
		return toHexString(certID);
	}

	/**
	 * Gets the modulus byte array from the RSA private key and calculates the
	 * SHA-1 digest and return a hex string representation of the SHA-1.
	 *
	 * @param rsaPrivateKey
	 * @return String
	 */
	public static String getPrivateKeyModulusSHA1(final RSAPrivateKey rsaPrivateKey) {
		byte[] keyModulusData = rsaPrivateKey.getModulus().toByteArray();
		byte[] keyID = DigestUtils.sha1(keyModulusData);
		return toHexString(keyID);
	}

	/**
	 * Checks whether the X.509 certificates public key was generated from the
	 * given private key
	 *
	 * @param certificateText
	 * @param privateKeyText
	 * @return
	 * @throws java.security.GeneralSecurityException
	 * @throws java.io.IOException
	 */
	public static boolean isCertificateValid(final String certificateText, final String privateKeyText) throws GeneralSecurityException, IOException {
		return isCertificateValid(certificateText, privateKeyText, null);
	}

	/**
	 * Checks whether the X.509 certificates public key was generated from the
	 * given RSA private key
	 *
	 * @param certificateText
	 * @param privateKeyText
	 * @param privateKeyPassword
	 * @return
	 * @throws java.security.GeneralSecurityException
	 * @throws java.io.IOException
	 */
	public static boolean isCertificateValid(final String certificateText, final String privateKeyText, final char[] privateKeyPassword)
			throws GeneralSecurityException, IOException {
		RSAPrivateKey pk = parseRSAPrivateKey(privateKeyText, privateKeyPassword);
		X509Certificate certificate = parseX509Certificate(certificateText);
		return isCertificateValid(certificate, pk);
	}

	/**
	 * Checks whether the X.509 certificates public key was generated from the
	 * given RSA private key
	 *
	 * @param cert
	 * @param privateKey
	 * @return
	 * @throws java.security.NoSuchAlgorithmException
	 */
	public static boolean isCertificateValid(final X509Certificate cert, final RSAPrivateKey privateKey) throws NoSuchAlgorithmException {
		String certIDinHex = getCertificateModulusSHA1(cert);
		String keyIDinHex = getPrivateKeyModulusSHA1(privateKey);
		return certIDinHex.equalsIgnoreCase(keyIDinHex);
	}

	/**
	 * returns an instance of PasswordFinder containing the given password
	 *
	 * @param password
	 * @return PasswordFinder
	 */
	public static PasswordFinder getPasswordFinder(final String password) {
		return () -> password.toCharArray();
	}

	/**
	 * returns an instance of PasswordFinder containing the given password
	 *
	 * @param password
	 * @return PasswordFinder
	 */
	public static PasswordFinder getPasswordFinder(final char[] password) {
		return () -> password;
	}

	/**
	 *
	 * @param privateKeyText
	 * @return
	 * @throws java.io.IOException
	 * @throws java.security.GeneralSecurityException
	 */
	public static String getDkimDnsTxt(final String privateKeyText) throws IOException, GeneralSecurityException {
		StringBuilder sb = new StringBuilder();
		sb.append("v=DKIM1; k=rsa; ");
		String cleanedPem = cleanRSAPrivateKeyPem(privateKeyText);
		PublicKey pk = getPublicKeyFromPrivateKey(cleanedPem.getBytes(), null);
		String key = pemWriter(pk);

		key = key.replace("-----BEGIN PUBLIC KEY-----", "");
		key = key.replace("-----END PUBLIC KEY-----", "");
		key = key.replace("\r", "");
		key = key.replace("\n", "");

		sb.append("p=").append(key);

		return sb.toString();
	}

	/**
	 *
	 * @param keysize
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyPair generateKeyPair(int keysize) throws NoSuchAlgorithmException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", new BouncyCastleProvider());
		keyGen.initialize(keysize, new SecureRandom());
		return keyGen.generateKeyPair();
	}

	/**
	 *
	 * @param privateKeyText
	 * @return
	 */
	public static String cleanRSAPrivateKeyPem(final String privateKeyText) {
		String privatePem = privateKeyText;
		String header = "-----BEGIN RSA PRIVATE KEY-----\n";
		String footer = "-----END RSA PRIVATE KEY-----\n";

		privatePem = privatePem.replace("-----BEGIN RSA PRIVATE KEY-----", "");
		privatePem = privatePem.replace("-----END RSA PRIVATE KEY-----", "");
		privatePem = privatePem.replace("\n", "");

		StringBuilder sb = new StringBuilder();
		sb.append(header);
		int index = 0;
		while (index < privatePem.length()) {
			sb.append(privatePem, index, Math.min(index + 64, privatePem.length())).append("\n");
			index += 64;
		}
		sb.append(footer);
		return sb.toString();
	}

	private static PrivateKey generatePrivateKeyByPEM(String privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		PrivateKey privKey;
		String privatePem = privateKey;

		privatePem = privatePem.replace("-----BEGIN RSA PRIVATE KEY-----", "");
		privatePem = privatePem.replace("-----END RSA PRIVATE KEY-----", "");
		privatePem = privatePem.replace("\n", "");

		byte[] encoded = Base64.decode(privatePem);

		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);

		KeyFactory kf = KeyFactory.getInstance("RSA");
		privKey = kf.generatePrivate(keySpec);

		return privKey;
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	/**
	 * returns a hex representation of the byte array
	 *
	 * @param bytes
	 * @return String
	 */
	public static String toHexString(final byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	/**
	 *
	 * @param o
	 * @return
	 * @throws IOException
	 */
	public static String pemWriter(Object o) throws IOException {
		StringWriter sw = new StringWriter();
		PEMWriter pw = new PEMWriter(sw);
		pw.writeObject(o);
		pw.flush();
		return sw.toString();
	}

	/**
	 *
	 * @param pem
	 * @return
	 * @throws IOException
	 */
	public static Object pemReader(String pem) throws IOException {
		StringReader reader = new StringReader(pem);
		PEMReader pr = new PEMReader(reader);
		return pr.readObject();
	}
}
