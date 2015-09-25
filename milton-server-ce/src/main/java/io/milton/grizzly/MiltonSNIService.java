/* 
 *       Copyright FuseLMS
 */
package io.milton.grizzly;

import org.glassfish.grizzly.PortRange;
import org.glassfish.grizzly.filterchain.Filter;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.sni.SNIFilter;
import org.glassfish.grizzly.sni.SNIServerConfigResolver;
import org.glassfish.grizzly.ssl.SSLBaseFilter;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Lee YOU
 */
public class MiltonSNIService {

    private static final Logger log = LoggerFactory.getLogger(MiltonSNIService.class);

    public static final int SECURE_PORT = 8443;
    public static final String SYS_SECURE_PORT = "secure.port";
    public static final String SNI_LISTENER_NAME = "SNI_SSL_TLS";

    private int port = SECURE_PORT;

    private SNIFilter sniFilter = null;
    private TCPNIOTransport transport = null;
    private NetworkListener listenerSSL = null;
    private SSLEngineConfigurator sslDefEngineConfig = null;

    public MiltonSNIService(int port, SSLEngineConfigurator sslEngineConfigurator) {
        if (sslEngineConfigurator == null) {
            // TODO  need to create a default SSL EngineConfig
        } else {
            this.sslDefEngineConfig = sslEngineConfigurator;
        }

        this.port = port;
    }

    public void start(FilterChain filterChain, SNIServerConfigResolver sniServerConfigResolver) {

        this.sniFilter = new SNIFilter();
        this.sniFilter.setServerSSLConfigResolver(sniServerConfigResolver);

        FilterChainBuilder chainBuilder = FilterChainBuilder.stateless();
        chainBuilder.add(new TransportFilter());
        chainBuilder.add(this.sniFilter);

        Filter sslFilter = null;
        for (int i = 0; i < filterChain.size(); i++) {
            sslFilter = filterChain.get(i);

            if (sslFilter instanceof TransportFilter) {
                continue;
            }

            chainBuilder.add(sslFilter);
            //log.info(" filter added " + sslFilter);
        }

        FilterChain filterChainNew = chainBuilder.build();
        this.transport = TCPNIOTransportBuilder.newInstance().setProcessor(filterChainNew)
                .build();
        try {
            this.transport.bind(port);
            this.transport.start();

            log.info("[HTTPS Service](SNI) start on port= " + port);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void shutdown() {
        if (this.transport != null) {
            this.transport.shutdown();
        }
        if (this.listenerSSL != null) {
            this.listenerSSL.shutdown();
        }
    }

    public void startOn(HttpServer httpServer) {
        if (this.sslDefEngineConfig == null || httpServer == null) {
            return;
        }

        this.listenerSSL = new NetworkListener(SNI_LISTENER_NAME, NetworkListener.DEFAULT_NETWORK_HOST, new PortRange(this.port));
        this.listenerSSL.setSSLEngineConfig(this.sslDefEngineConfig);
        this.listenerSSL.setSecure(true);
        httpServer.addListener(this.listenerSSL);
        log.info("[HTTPS Service](SNI) start on port= " + port);
    }

    public boolean setupSNI(FilterChain filterChain, SNIServerConfigResolver sniServerConfigResolver) {
        log.info("setupSNI and filterChain is:" + filterChain);

        if (filterChain == null) {
            return false;
        }

        if (this.sniFilter == null) {
            this.sniFilter = new SNIFilter();
        }

        if (sniServerConfigResolver instanceof SNIServerConfigResolver) {
            log.info("setupSNI and sniServerConfigResolver is:" + sniServerConfigResolver);
            this.sniFilter.setServerSSLConfigResolver(sniServerConfigResolver);
        }

        Filter sslFilter = null;
        for (int i = 0; i < filterChain.size(); i++) {
            sslFilter = filterChain.get(i);

            if (sslFilter instanceof TransportFilter) {
                log.info("setup SNI and TransportFilter is removed. ");
                filterChain.remove(i--);
                continue;
            }

            if (sslFilter instanceof SSLBaseFilter) {
                log.info("setup SNI and SSLBaseFilter is changed to be " + this.sniFilter);
                filterChain.set(i, this.sniFilter);
            }
        }

        log.info("setupSNI: not found the sslFilter in FilterChain");
        return false;
    }
}
