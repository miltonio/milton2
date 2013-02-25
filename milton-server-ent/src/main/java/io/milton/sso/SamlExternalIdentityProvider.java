/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * 
 */

package io.milton.sso;

import io.milton.http.Request;
import io.milton.resource.Resource;
import io.milton.http.Response;
import io.milton.common.StreamUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.UUID;
import javax.xml.namespace.QName;
//import org.joda.time.DateTime;
//import org.opensaml.Configuration;
//import org.opensaml.DefaultBootstrap;
//import org.opensaml.common.binding.BasicSAMLMessageContext;
//import org.opensaml.saml2.binding.encoding.HTTPRedirectDeflateEncoder;
//import org.opensaml.saml2.core.AuthnContextClassRef;
//import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
//import org.opensaml.saml2.core.AuthnRequest;
//import org.opensaml.saml2.core.Issuer;
//import org.opensaml.saml2.core.NameIDPolicy;
//import org.opensaml.saml2.core.NameIDType;
//import org.opensaml.saml2.core.RequestedAuthnContext;
//import org.opensaml.saml2.metadata.AssertionConsumerService;
//import org.opensaml.saml2.metadata.Endpoint;
//import org.opensaml.ws.message.encoder.MessageEncodingException;
//import org.opensaml.xml.ConfigurationException;
//import org.opensaml.xml.XMLConfigurator;
//import org.opensaml.xml.XMLObjectBuilder;
//import org.opensaml.xml.XMLObjectBuilderFactory;
//import org.opensaml.xml.parse.BasicParserPool;
//import org.opensaml.xml.parse.XMLParserException;
//import org.opensaml.xml.security.credential.Credential;
//import org.opensaml.xml.security.x509.BasicX509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 *
 * @author brad
 */
public class SamlExternalIdentityProvider { //implements ExternalIdentityProvider {

	private static final Logger log = LoggerFactory.getLogger(SamlExternalIdentityProvider.class);
	public static final String AUTHN_LOW_STRENGH = "urn:nzl:govt:ict:stds:authn:deployment:GLS:SAML:2.0:ac:classes:LowStrength";
	private String name;
	private String loginUrl;
//	private XMLObjectBuilderFactory builderFactory;
//	private BasicParserPool parserPool;
//	private String issuer = "http://localhost:8080/";
//	private String nameFormat = NameIDType.PERSISTENT;
//	private String authStrength = AUTHN_LOW_STRENGH;
//	private AuthnContextComparisonTypeEnumeration authComparison = AuthnContextComparisonTypeEnumeration.EXACT;
//	private Credential localCred;  // initialised in cstr
//
//	/**
//	 * 
//	 * @param keystoreFile - the keystore file
//	 * @param localAlias - the alias for the local certificate
//	 * @param keystorePassword - password to open keystore
//	 * @param localPassword - password to access local alias
//	 * 
//	 * @throws XMLParserException
//	 * @throws ConfigurationException
//	 * @throws KeyStoreException
//	 * @throws FileNotFoundException
//	 * @throws IOException
//	 * @throws NoSuchAlgorithmException
//	 * @throws CertificateException
//	 * @throws UnrecoverableEntryException 
//	 */
//	public SamlExternalIdentityProvider(File keystoreFile, String localAlias, String keystorePassword, String localPassword) throws XMLParserException, ConfigurationException, KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException {
//		if( keystoreFile == null ) {
//			throw new RuntimeException("keystore file was null");
//		}
//		// Initialize the OpenSAML library
//		try {
//			javax.xml.parsers.SAXParserFactory spFactory = javax.xml.parsers.SAXParserFactory.newInstance();
//			spFactory.setNamespaceAware(true);
//			DefaultBootstrap.bootstrap();
//		} catch (ConfigurationException e) {
//			throw new RuntimeException(e);
//		}
//
//		parserPool = new BasicParserPool();
//		parserPool.setNamespaceAware(true);
//		// SOAP 1.1 Configuration
//		Document soap11Config = parserPool.parse(SamlExternalIdentityProvider.class.getResourceAsStream("/soap11-config.xml"));
//		Document defaulfConfig = parserPool.parse(SamlExternalIdentityProvider.class.getResourceAsStream("/default-config.xml"));
//
//		XMLConfigurator configurator = new XMLConfigurator();
//		configurator.load(soap11Config);
//		configurator.load(defaulfConfig);
//
//		builderFactory = Configuration.getBuilderFactory();
//		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
//		FileInputStream fin = null;
//		try {
//			fin = new FileInputStream(keystoreFile);
//			ks.load(fin, toCharArray(keystorePassword));
//		} finally {
//			StreamUtils.close(fin);
//		}
//		KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(localAlias, new KeyStore.PasswordProtection(toCharArray(localPassword)));
//		if (keyEntry == null) {
//			log.error("No keystore entry named: " + localAlias + " listing available aliases..");
//			Enumeration<String> aliasesEn = ks.aliases();
//			while (aliasesEn.hasMoreElements()) {
//				String alias = aliasesEn.nextElement();
//				log.error("Available alias: " + alias);
//			}
//			throw new RuntimeException("Couldnt initialise keystore. See previous messages for available aliases");
//		}
//		PrivateKey privateKey = keyEntry.getPrivateKey();
//		if (privateKey == null) {
//			throw new RuntimeException("No private key associated with " + keyEntry.toString());
//		} else {
//			System.out.println("using PK: " + privateKey);
//		}
//		localCred = new BasicX509Credential();
//		((BasicX509Credential) localCred).setPrivateKey(privateKey);
//
//	}
//
//	private char[] toCharArray(String value) {
//		if (value == null) {
//			return null;
//		}
//		return value.toCharArray();
//	}
//
//	@Override
//	public String getName() {
//		return name;
//	}
//
//	@Override
//	public void initiateExternalAuth(Resource resource, Request request, Response response) {
////		response.sendRedirect(loginUrl);
//
//		AuthnRequest auth = createSAMLObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
//		auth.setIssueInstant(new DateTime());
//		auth.setID(UUID.randomUUID().toString());
//		auth.setIssuer(createIssuer(issuer));
//		auth.setForceAuthn(true);
//		auth.setIsPassive(false);
//		auth.setAssertionConsumerServiceIndex(0);
//
//		auth.setDestination(loginUrl);
//		auth.setNameIDPolicy(createNameIDPolicy(nameFormat, true));
//		auth.setRequestedAuthnContext(createRequestedAuthnContext(authComparison, authStrength));
//
//		MiltonHttpOutTransport transport = new MiltonHttpOutTransport(request, response);
//
//		BasicSAMLMessageContext messageContext = new BasicSAMLMessageContext();
//		messageContext.setOutboundMessageTransport(transport);
//		messageContext.setOutboundSAMLMessage(auth);
//		messageContext.setPeerEntityEndpoint(createEndPoint(loginUrl));
//
//		messageContext.setOutboundSAMLMessageSigningCredential(localCred);
//		System.out.println("set signing cred: " + localCred.getEntityId() + " - " + localCred);
//		System.out.println("local cred pk: " + localCred.getPrivateKey());
//
//		String state = UUID.randomUUID().toString();
//		messageContext.setRelayState(state);
//
//		HTTPRedirectDeflateEncoder encoder = new HTTPRedirectDeflateEncoder();
//		try {
//			encoder.encode(messageContext);
//		} catch (MessageEncodingException ex) {
//			throw new RuntimeException(ex);
//		}
//
//
//	}
//
//	private Issuer createIssuer(String issuerStr) {
//		Issuer issuerEl = createSAMLObject(Issuer.DEFAULT_ELEMENT_NAME);
//		issuerEl.setValue(issuerStr);
//		System.out.println("issuer: " + issuerStr);
//		return issuerEl;
//	}
//
//	public NameIDPolicy createNameIDPolicy(String nameFormat, boolean allowCreate) {
//		NameIDPolicy policy = createSAMLObject(NameIDPolicy.DEFAULT_ELEMENT_NAME);
//		policy.setFormat(nameFormat);
//		policy.setAllowCreate(allowCreate);
//		return policy;
//	}
//
//	public Endpoint createEndPoint(String url) {
//		Endpoint ep = createSAMLObject(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
//		ep.setLocation(url);
//		return ep;
//	}
//
//	public RequestedAuthnContext createRequestedAuthnContext(AuthnContextComparisonTypeEnumeration comparison, String ctxClass) {
//		RequestedAuthnContext ctx = createSAMLObject(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
//		ctx.setComparison(comparison);
//		AuthnContextClassRef ctxClassRef = createSAMLObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
//		ctxClassRef.setAuthnContextClassRef(ctxClass);
//		ctx.getAuthnContextClassRefs().add(ctxClassRef);
//		return ctx;
//	}
//
//	public String getLoginUrl() {
//		return loginUrl;
//	}
//
//	public void setLoginUrl(String loginUrl) {
//		this.loginUrl = loginUrl;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}
//
//	public String getIssuer() {
//		return issuer;
//	}
//
//	public void setIssuer(String issuer) {
//		this.issuer = issuer;
//	}
//
//	@SuppressWarnings("unchecked")
//	private <T> T createSAMLObject(QName qname) {
//		XMLObjectBuilder builder = builderFactory.getBuilder(qname);
//		if (builder == null) {
//			throw new RuntimeException("Builder not found for " + qname);
//		}
//		return (T) builder.buildObject(qname);
//	}
//
//	public Credential getLocalPrivate() {
//		return localCred;
//	}
}
