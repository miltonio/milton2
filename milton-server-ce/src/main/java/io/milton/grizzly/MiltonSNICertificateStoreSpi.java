/*
 *       Copyright FuseLMS
 */
package io.milton.grizzly;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStoreSpi;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a Certificate Store Spi for to load/save the certificates and PrivateKey
 * from/into the same or even different database/files by the specific host
 * name.
 *
 * @author Lee YOU
 */
public class MiltonSNICertificateStoreSpi extends KeyStoreSpi {

    private static final Logger log = LoggerFactory.getLogger(MiltonSNICertificateStoreSpi.class);

    private final MiltonSNICertificateStore store;

    public MiltonSNICertificateStoreSpi(MiltonSNICertificateStore store) {
        this.store = store;
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public X509Certificate[] engineGetCertificateChain(String hostName) {
        this.checkHostName(hostName);
        return store.getCertificatesFromDb(hostName);
    }

    @Override
    public Certificate engineGetCertificate(String hostName) {
        log.trace("engineGetCertificate and the domainName is: {}", hostName);
        Certificate[] cf = this.engineGetCertificateChain(hostName);

        if (cf != null && cf.length > 0) {
            return cf[0];
        }
        return null;
    }

    @Override
    public Key engineGetKey(String alias, char[] password) {
        log.trace("engineGetCertificate and the domainName is: {}" , alias);
        this.checkHostName(alias);

        PrivateKey pk = store.loadPrivateKeyFromDb(alias, password);

        if (pk != null) {
            log.trace(" the PrivateKey is loaded.");
            return pk;
        }

        return null;
    }

    @Override
    public boolean engineContainsAlias(String hostName) {
        this.checkHostName(hostName);

        return engineIsKeyEntry(hostName) && engineIsCertificateEntry(hostName);
    }

    @Override
    public boolean engineIsKeyEntry(String hostName) {
        //only be used/supported when PK is in the differnt database/store
        return true;//store.containsAlias(hostName);
    }

    @Override
    public boolean engineIsCertificateEntry(String hostName) {
        return store.containsAlias(hostName);
    }

    private void checkHostName(String hostName) {
        if (hostName == null) {
            throw new NullPointerException("hostName == null");
        }
    }

    //Unused interfaces in this SNI service
    @Override
    public String engineGetCertificateAlias(Certificate cf) {
        return store.getCertificateAlias(cf);
    }

    @Override
    public Date engineGetCreationDate(String alias) {
        return store.getCreationDate(alias);
    }

    @Override
    public Enumeration<String> engineAliases() {
        return Collections.enumeration(store.aliases());
    }

    @Override
    public int engineSize() {
        return store.aliases().size();
    }

    // Unsupported interfaces in this SNI service cause the certificates are stored into the database using UI APIs
    @Override
    public void engineStore(OutputStream stream, char[] password) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void engineLoad(InputStream stream, char[] password) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void engineSetCertificateEntry(String alias, Certificate cert) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void engineDeleteEntry(String alias) {
        throw new UnsupportedOperationException();
    }

}
