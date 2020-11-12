/*
 *       Copyright FuseLMS
 */
package io.milton.grizzly;

import static io.milton.grizzly.GrizzlyServer.getPropertyOrDefault;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.StandardConstants;
import javax.net.ssl.X509ExtendedKeyManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a Certificate Manager providing the certificates and PrivateKey which located
 * in the database for supporting the SNI service to switch to the matched host
 * by the specific host name ,when the SSL request is coming, before the
 * connection handshaking.
 *
 * @author Lee YOU
 */
public class MiltonSNICertificateManager {

    private static final Logger log = LoggerFactory.getLogger(MiltonSNICertificateManager.class);
    public static final String SECURE_TYPE = "TLS";
    public static final String SYS_SECURE_PROTOCOL = "secure.protocol";
    public final String primaryDomain;

    private static MiltonSNICertificateManager sniCerManager;

    private final MiltonSNICertificateStoreSpi keyStoreSpi;
    private final MiltonSNICertificateStore certificateStore;

    public MiltonSNICertificateManager( MiltonSNICertificateStore store) {
        this.certificateStore = store;
        this.keyStoreSpi = new MiltonSNICertificateStoreSpi(this.certificateStore);
        this.primaryDomain = getPropertyOrDefault("secure.primary_domain", "localhost");
    }

    public KeyManager[] createKeyManager() {
        return new SNICertificateManager[]{new SNICertificateManager()};
    }

    public SSLEngineConfigurator createEngineConfigurator() {
        return this.createEngineConfigurator(this.createKeyManager());
    }

    public SSLEngineConfigurator createEngineConfigurator(KeyManager[] keyManager) {
        try {
            String protocol = getPropertyOrDefault(SYS_SECURE_PROTOCOL, SECURE_TYPE);
            SSLContext sslContext = SSLContext.getInstance(protocol);
            sslContext.init(keyManager, null, new SecureRandom());

            return new SSLEngineConfigurator(sslContext, false, false, false);
        } catch (Exception ex) {
            log.error("createSSLContext", ex);
        }

        return null;
    }

    /*
     *
     */
    protected class SNICertificateManager extends X509ExtendedKeyManager {

        @Override
        public X509Certificate[] getCertificateChain(String domainName) {
            log.trace("Get the CertificateChain for the domain {}", domainName);

            X509Certificate[] cer;

            if (domainName.endsWith("admin." + primaryDomain)) {
                cer = loadCertificatesFromLocalFile();
            } else {
                cer = keyStoreSpi.engineGetCertificateChain(domainName);
            }
            if( log.isDebugEnabled()) {
                log.debug("the Certificate Chain is : {}" , Arrays.toString(cer));
            }
            return cer;
        }

        @Override
        public PrivateKey getPrivateKey(String domainName) {
            log.trace("Get the PrivateKey for the domain : {}", domainName);

            PrivateKey pk = null;
            if (domainName.endsWith("admin." + primaryDomain)) {
                try {
                    pk = loadPrivateKeyFromFile();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                pk = (PrivateKey) keyStoreSpi.engineGetKey(domainName, null);
            }
            log.trace("Retrieved PK: {}", pk);
            return pk;
        }

        private PrivateKey loadPrivateKeyFromFile() throws IOException, GeneralSecurityException {
            String filePath = getPropertyOrDefault("secure.privatekey", null);
            if (StringUtils.isNotBlank(filePath)) {
                File file = new File(filePath).getAbsoluteFile();
                byte[] certBytes = FileUtils.readFileToByteArray(file);
                return SSLTools.parsePrivateKey(certBytes);
            }

            return null;
        }

        /**
         * Load the certificates from the .crt file.
         *
         * @return
         */
        private X509Certificate[] loadCertificatesFromLocalFile() {
            try {
                List<File> certFiles = getCertificateFiles();
                List<X509Certificate> certificates = new ArrayList<>();

                for (File certFile : certFiles) {
                    if (certFile.isFile() && certFile.exists()) {
                        byte[] certBytes = FileUtils.readFileToByteArray(certFile);
                        X509Certificate cert = SSLTools.parseX509Certificate(certBytes);
                        if (cert != null) {
                            certificates.add(cert);
                        }
                    }
                }

                X509Certificate[] xcf = new X509Certificate[certificates.size()];
                certificates.toArray(xcf);
                log.trace("loaded X509Certificate[] len = {}", xcf.length);
                return xcf;
            } catch (Exception ex) {
                log.error("getCertificate error.", ex);
            }
            return null;
        }

        private List<File> getCertificateFiles() throws URISyntaxException {
            List<File> certificate = new ArrayList<>();
            String pathToCerts = getPropertyOrDefault("secure.certificate", null);
            String[] certParts = pathToCerts.split(",");
            for (String certPath : certParts) {
                File certFile = new File(certPath);
                certificate.add(certFile);
            }
            return certificate;
        }

        @Override
        public String chooseEngineServerAlias(final String type, Principal[] issuers, SSLEngine engine) {
            log.trace("Https (SSL/TLS) Handshaking start....");
            log.trace("Choose the EngineServer Alias Name, and the engine type is: {}", type);

            // Get the Host Name of the SNI Server for this SSL request from the browser/client
            ExtendedSSLSession handshakeSession = (ExtendedSSLSession) engine.getHandshakeSession();
            log.debug("Choose EngineServer Alias Name, and the handshake session is:" + handshakeSession.hashCode());

            String domainName = null;
            for (SNIServerName name : handshakeSession.getRequestedServerNames()) {
                if (name.getType() == StandardConstants.SNI_HOST_NAME) {
                    domainName = ((SNIHostName) name).getAsciiName();
                    break;
                }
            }
            log.trace("chooseEngineServerAlias, SNIServerName is: {}" , domainName);

            // Check the hostname is existed or not form the certificate store(database)
            boolean hostNameCheck = keyStoreSpi.engineContainsAlias(domainName);
            if (hostNameCheck) {
                return domainName;
            }
            return null;
        }

        // Unsupported for the client side checking only
        @Override
        public String[] getClientAliases(String alias, Principal[] prncpls) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String chooseClientAlias(String[] alias, Principal[] prncpls, Socket socket) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String chooseEngineClientAlias(String[] alias, Principal[] issuers, SSLEngine engine) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        // Uselessed interface in the new JDK
        @Override
        public String[] getServerAliases(String string, Principal[] prncpls) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String chooseServerAlias(String string, Principal[] prncpls, Socket socket) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
