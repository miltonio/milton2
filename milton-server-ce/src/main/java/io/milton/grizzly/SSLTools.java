/*
 *       Copyright FuseLMS
 */
package io.milton.grizzly;

import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
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
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

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
	 * @throws java.io.IOException
	 */
	public static PrivateKey parsePrivateKey(final byte[] privateKeyBytes, final char[] password) throws IOException, GeneralSecurityException {

		try (ByteArrayInputStream bais = new ByteArrayInputStream(privateKeyBytes);
			 InputStreamReader reader = new InputStreamReader(bais)) {

			PEMParser keyReader = new PEMParser(reader);
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
			Object keyPair = keyReader.readObject();
			PrivateKeyInfo keyInfo;
			if (keyPair instanceof PEMEncryptedKeyPair) {
				PEMDecryptorProvider decryptionProv = new JcePEMDecryptorProviderBuilder().build(password);
				PEMKeyPair decryptedKeyPair = ((PEMEncryptedKeyPair) keyPair).decryptKeyPair(decryptionProv);
				keyInfo = decryptedKeyPair.getPrivateKeyInfo();
			} else if (keyPair instanceof PKCS8EncryptedPrivateKeyInfo) {
				try {
					InputDecryptorProvider decryptionProv = new JceOpenSSLPKCS8DecryptorProviderBuilder().build(password);
					keyInfo = ((PKCS8EncryptedPrivateKeyInfo) keyPair).decryptPrivateKeyInfo(decryptionProv);
				} catch (Exception ex) {
					throw new GeneralSecurityException(ex);
				}
			} else {
				keyInfo = ((PEMKeyPair) keyPair).getPrivateKeyInfo();
			}
			keyReader.close();
			return converter.getPrivateKey(keyInfo);
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
		PEMParser pemParser = new PEMParser(new InputStreamReader(bais));
		Object object = pemParser.readObject();
		pemParser.close();
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
		KeyPair kp;
		if (object == null) {
			throw new IllegalStateException("PEM parsing failed: missing or invalid data");
		} else if (object instanceof PKCS8EncryptedPrivateKeyInfo) {
			if (password == null)
				throw new GeneralSecurityException("passphrase required");
			try {
				InputDecryptorProvider decryptionProv = new JceOpenSSLPKCS8DecryptorProviderBuilder().build(password.toCharArray());
				final PrivateKeyInfo privateKeyInfo = ((PKCS8EncryptedPrivateKeyInfo) object).decryptPrivateKeyInfo(decryptionProv);
				PublicKey pub;
				ASN1ObjectIdentifier id = privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm();
				PKCS8EncodedKeySpec p8s = new PKCS8EncodedKeySpec(privateKeyInfo.getEncoded());
				if (id.equals(PKCSObjectIdentifiers.rsaEncryption)) {
					KeyFactory rfact = KeyFactory.getInstance("RSA");
					RSAPrivateCrtKey rprv = (RSAPrivateCrtKey) rfact.generatePrivate(p8s);
					pub = rfact.generatePublic(new RSAPublicKeySpec(rprv.getModulus(), rprv.getPublicExponent()));
				} else if (id.equals(X9ObjectIdentifiers.id_dsa)) {
					KeyFactory dfact = KeyFactory.getInstance("DSA");
					DSAPrivateKey dprv = (DSAPrivateKey) dfact.generatePrivate(p8s);
					BigInteger p = dprv.getParams().getP();
					BigInteger q = dprv.getParams().getQ();
					BigInteger g = dprv.getParams().getG();
					pub = dfact.generatePublic (new DSAPublicKeySpec(g.modPow(dprv.getX(),p), p, q, g));
				} else if (id.equals(X9ObjectIdentifiers.id_ecPublicKey)) {
					ECPrivateKey eprv = ECPrivateKey.getInstance(privateKeyInfo.parsePrivateKey());
					byte[] eenc = new SubjectPublicKeyInfo(privateKeyInfo.getPrivateKeyAlgorithm(), eprv.getPublicKey().getOctets()).getEncoded();
					pub = KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(eenc));
				} else {
					throw new GeneralSecurityException ("unknown private key OID " + id);
				}
				return new KeyPair(pub, converter.getPrivateKey(privateKeyInfo));
			} catch (Exception ex) {
				throw new GeneralSecurityException(ex);
			}
		} else if (object instanceof PEMEncryptedKeyPair) {
			if (password == null)
				throw new GeneralSecurityException("passphrase required");
			try {
				PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
				kp = converter.getKeyPair(((PEMEncryptedKeyPair) object).decryptKeyPair(decProv));
			} catch (Exception e) {
				throw new GeneralSecurityException("wrong passphrase", e);
			}
		} else if (object instanceof PEMKeyPair) {
			kp = converter.getKeyPair((PEMKeyPair) object);
		} else if (object instanceof PrivateKeyInfo) {
			PrivateKey privKey = converter.getPrivateKey((PrivateKeyInfo) object);
			kp = new KeyPair(null, privKey);
		} else {
			throw new IllegalStateException("PEM parser support missing for: " + object);
		}
		return kp;
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
		JcaPEMWriter pw = new JcaPEMWriter(sw);
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
		PEMParser pr = new PEMParser(reader);
		return pr.readObject();
	}
}
