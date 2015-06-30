/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.milton.simpleton;

import io.milton.http.HttpManager;
import io.milton.http.http11.Http11ResponseHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.X509TrustManager;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Socket;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bradm
 */
public class SslSimpletonServer implements Container {

    private static final Logger log = LoggerFactory.getLogger( SslSimpletonServer.class );
    public static final String DEFAULT_KEYSTORE_ALGORITHM = ( Security.getProperty( "ssl.KeyManagerFactory.algorithm" ) == null ? "SunX509" : Security.getProperty( "ssl.KeyManagerFactory.algorithm" ) );
    protected final Stage<Task> dispatchStage;
	private final HttpManager httpManager;
    private final Http11ResponseHandler responseHandler;
    private int sslPort = 443;
    private File keystoreFile;
    private String keystorePassword;
    private Thread thMonitor;
    private boolean stopped;
    private int maxQueueTimeMillis = 10000;
    private int maxProcessTimeMillis = 60000;
    private Connection connection;
    private Connection sslConnection;
    private String sslProtocol = "TLS";
    private String keystoreType = "JKS";
    private String keystoreAlgorithm = DEFAULT_KEYSTORE_ALGORITHM;


    public SslSimpletonServer(HttpManager httpManager, Http11ResponseHandler responseHandler, int capacity, int numThreads) {
		this.httpManager = httpManager;
        dispatchStage = new Stage<Task>("dispatchStage", capacity, numThreads, false);
        this.responseHandler = responseHandler;
		thMonitor = new Thread(new TaskMonitor());
    }

    public void start() {
        stopped = false;

        sslConnection = initHttps( sslPort );

        thMonitor = new Thread( new TaskMonitor() );
        thMonitor.start();
    }


    /**
     * Setting up certificates
    EG C:\Program Files\Java\jdk1.6.0_10\bin>keytool -genkey -keystore certs -keyalg rsa -alias jamie -storepass serverkspw -keypass serverpw
    note that 'first name last name' should be machine name

     * @param connection
     * @param port
     */
    protected SocketConnection initHttps( int port ) {
        SSLServerSocketFactory fac = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        log.info( "initHttps: port: " + port + " sslProtocol: " + sslProtocol + " keystoreAlgorithm:" + keystoreAlgorithm );        
        try {
            KeyStore keystore = KeyStore.getInstance( keystoreType );
            keystore.load( new FileInputStream( keystoreFile ), keystorePassword.toCharArray() );
            log.info( "listing aliases defined in keystore" );
            Enumeration<String> aliases = keystore.aliases();
            while( aliases.hasMoreElements() ) {
                String a = aliases.nextElement();
                log.info( " - alias: " + a );
                Certificate cert = keystore.getCertificate( a );
                log.info("   - cert type: " + cert.getType());
                log.info("   - algorithm: " + cert.getPublicKey().getAlgorithm() );
                log.info("   - format: " + cert.getPublicKey().getFormat() );
            }
            

            KeyManagerFactory kmf = KeyManagerFactory.getInstance( keystoreAlgorithm );
            kmf.init( keystore, keystorePassword.toCharArray() );

            X509TrustManager trustManager = new AnonymousTrustManager();
            X509TrustManager[] trustManagers = new X509TrustManager[]{trustManager};

            SSLContext sslc = SSLContext.getInstance( sslProtocol ); // An SSLContext is an environment for implementing JSSE. It is used to create a ServerSocketFactory
            sslc.init( kmf.getKeyManagers(), trustManagers, null );


            ContainerServer processor = new ContainerServer(this, 25);
            org.simpleframework.transport.Server secure = new SecureProcessor(processor, sslc);
            SocketConnection ssl = new SocketConnection(secure);
            InetSocketAddress address = new InetSocketAddress( port );
            ssl.connect( address, sslc );

            log.debug( "server running on: " + address );
            
            return ssl;
        } catch( java.net.BindException ex ) {
            throw new RuntimeException( "Couldnt bind to port: " + port );
        } catch( KeyManagementException ex ) {
            throw new RuntimeException( ex );
        } catch( UnrecoverableKeyException ex ) {
            throw new RuntimeException( ex );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        } catch( NoSuchAlgorithmException ex ) {
            throw new RuntimeException( ex );
        } catch( CertificateException ex ) {
            throw new RuntimeException( ex );
        } catch( KeyStoreException ex ) {
            throw new RuntimeException( ex );
        } catch( Exception ex ) {
            throw new RuntimeException( ex );
        }
    }

    public void stop() {
        try {
            dispatchStage.close();
        } catch( IOException ex ) {
            log.error( "exception closing dispatchStage", ex );
        }
        stopped = true;
        thMonitor.interrupt();

        if( connection != null ) {
            try {
                connection.close();
            } catch( Exception ex ) {
                log.error( "exception closing http connection", ex );
            }
        }
        if( sslConnection != null ) {
            try {
                sslConnection.close();
            } catch( Exception ex ) {
                log.error( "exception closing ssl connection", ex );
            }
        }

    }

    @Override
    public void handle( Request request, Response response ) {
        Task task = new Task( httpManager, request, response );
        try {
            dispatchStage.enqueue( task );
        } catch( Exception e ) {
            log.warn( "exception dispatching request: " + e.getMessage() );
            SimpleMiltonRequest req = new SimpleMiltonRequest( request );
            SimpleMiltonResponse resp = new SimpleMiltonResponse( response );
            respondError( req, resp, e.getMessage() );
        }
    }

    public void respondError( SimpleMiltonRequest req, SimpleMiltonResponse resp, String reason ) {
        responseHandler.respondServerError( req, resp, reason );
    }

    private void respondError( Task t ) {
        try {
            log.warn( "setting error status becaue request could not be processed" );
            t.response.setCode( 500 );
//            t.response.commit();
            t.response.close();
        } catch( Exception e ) {
            log.error( "error setting last chance error status", e );
        }
    }

    public Integer getHttpPort() {
        return sslPort;
    }


    public void setHttpPort( Integer sslPort ) {
        this.sslPort = sslPort;
    }

    public File getKeystoreFile() {
        return keystoreFile;
    }

    public void setKeystoreFile( File keystoreFile ) {
        this.keystoreFile = keystoreFile;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword( String keystorePassword ) {
        this.keystorePassword = keystorePassword;
    }

    public String getSslProtocol() {
        return sslProtocol;
    }

    public void setSslProtocol( String sslProtocol ) {
        this.sslProtocol = sslProtocol;
    }

    public String getKeystoreAlgorithm() {
        return keystoreAlgorithm;
    }

    public void setKeystoreAlgorithm( String keystoreAlgorithm ) {
        this.keystoreAlgorithm = keystoreAlgorithm;
    }

    public String getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType( String keystoreType ) {
        this.keystoreType = keystoreType;
    }

    public int getMaxProcessTimeMillis() {
        return maxProcessTimeMillis;
    }

    public void setMaxProcessTimeMillis( int maxProcessTimeMillis ) {
        this.maxProcessTimeMillis = maxProcessTimeMillis;
    }

    public int getMaxQueueTimeMillis() {
        return maxQueueTimeMillis;
    }

    public void setMaxQueueTimeMillis( int maxQueueTimeMillis ) {
        this.maxQueueTimeMillis = maxQueueTimeMillis;
    }

    public class TaskMonitor implements Runnable {

        public void run() {
            boolean isInterrupted = false;
            while( !stopped && !isInterrupted ) {
                checkTasks();
                try {
                    Thread.sleep( 1000 );
                } catch( InterruptedException ex ) {
                    log.debug( "interrupted" );
                    isInterrupted = true;
                }
            }
        }
    }

    private void checkTasks() {
        long l;
        for( Task t : this.dispatchStage.queue ) {
            // check enqueue time
            l = System.currentTimeMillis() - t.enqueueTime;
            if( l > maxQueueTimeMillis ) {
                // bif it
                log.warn( "XXX task is too long in queue: " + l + "ms. " + t );
                log.warn( "Queue Size: " + dispatchStage.queue.size() );
                log.warn( "listing contents of queue -" );
                for( Task q : dispatchStage.queue ) {
                    log.warn( " - " + q.request.getTarget() );
                }
                log.warn( "---" );
                this.dispatchStage.queue.remove( t );
                respondError( t );
            } else {
                if( t.startTime > 0 ) {
                    // check process time
                    l = System.currentTimeMillis() - t.startTime;
                    if( l > maxProcessTimeMillis ) {
                        log.warn( "**** task is too long being processed: " + l + "ms. " + t );
                        t.thisThread.interrupt();
                    }
                }
            }
        }
    }

    public static class AnonymousTrustManager implements X509TrustManager {

        public boolean isClientTrusted( X509Certificate[] cert ) {
            return true;
        }

        public boolean isServerTrusted( X509Certificate[] cert ) {
            return true;
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        public void checkClientTrusted( X509Certificate[] arg0, String arg1 )
            throws CertificateException {
        }

        public void checkServerTrusted( X509Certificate[] arg0, String arg1 )
            throws CertificateException {
        }
    }

    private static class SecureProcessor implements org.simpleframework.transport.Server {

		private final ContainerServer processor;
		private final SSLContext context;

        public SecureProcessor( ContainerServer processor, SSLContext context ) {
            this.processor = processor;
            this.context = context;
        }

        public void process( Socket pipeline ) throws IOException {
            final SocketChannel channel = pipeline.getChannel();
            final Map map = new HashMap();
            Socket secure = new Socket() {

                private SSLEngine engine;

                public Map getAttributes() {
                    return map;
                }

                public SocketChannel getChannel() {
                    return channel;
                }

                public SSLEngine getEngine() {
                    if( engine == null ) {
                        engine = context.createSSLEngine();
                    }
                    return engine;
                }
            };
            processor.process( secure );
        }

        public void stop() {
        }
    }
}
