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

package io.milton.ldap;

import io.milton.http.webdav.PropFindPropertyBuilder;
import io.milton.http.webdav.WebDavProtocol;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LDAP server, handle LDAP directory requests.
 */
public class LdapServer extends Thread {

    private static final Logger log = LoggerFactory.getLogger(LdapServer.class);
    /**
     * Default LDAP port
     */
    public static final int DEFAULT_PORT = 389;
    private final UserFactory userSessionFactory;
    private final SearchManager searchManager;
    private final LdapTransactionManager txManager;
	private final PropFindPropertyBuilder propFindPropertyBuilder;
    protected boolean nosslFlag;
    private int port;
    private String bindAddress;
    private boolean allowRemote = true;
    private File keystoreFile;
    private String keystoreType;
    private String keystorePass;
    private ServerSocket serverSocket;

    /**
     * Create a ServerSocket to listen for connections. Start the thread.
     *
     * @param port pop listen port, 389 if not defined (0)
     */
    public LdapServer(LdapTransactionManager txManager, UserFactory userSessionFactory, int port, boolean nosslFlag, String bindAddress, PropFindPropertyBuilder propFindPropertyBuilder) {
        super(LdapServer.class.getName());
        this.txManager = txManager;
        searchManager = new SearchManager(txManager);
        setDaemon(true);
        if (port == 0) {
            this.port = LdapServer.DEFAULT_PORT;
        } else {
            this.port = port;
        }
        this.bindAddress = bindAddress;
        this.userSessionFactory = userSessionFactory;
        this.nosslFlag = nosslFlag;
		this.propFindPropertyBuilder = propFindPropertyBuilder;
    }

    public LdapServer(LdapTransactionManager txManager, UserFactory userSessionFactory, PropFindPropertyBuilder propFindPropertyBuilder) {
        super(LdapServer.class.getName());
        this.txManager = txManager;
        searchManager = new SearchManager(txManager);
        setDaemon(true);
        this.userSessionFactory = userSessionFactory;
		this.propFindPropertyBuilder = propFindPropertyBuilder;
        this.port = LdapServer.DEFAULT_PORT;
    }

    /**
     * This constructor is for convenience. It uses the list of property sources
     * from the WebDavProtocol object, freeing the developer from the need to
     * publicly declare property sources when only the built in ones are used.
     *
     * @param userSessionFactory
     * @param webDavProtocol
     */
    public LdapServer(LdapTransactionManager txManager, UserFactory userSessionFactory, WebDavProtocol webDavProtocol, PropFindPropertyBuilder propFindPropertyBuilder) {
        super(LdapServer.class.getName());
        this.txManager = txManager;
        searchManager = new SearchManager(txManager);
        setDaemon(true);
        this.userSessionFactory = userSessionFactory;
        this.port = LdapServer.DEFAULT_PORT;
		this.propFindPropertyBuilder = propFindPropertyBuilder;
    }

    public boolean isNosslFlag() {
        return nosslFlag;
    }

    public void setNosslFlag(boolean nosslFlag) {
        this.nosslFlag = nosslFlag;
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocolName() {
        return "LDAP";
    }

    public LdapConnection createConnectionHandler(Socket clientSocket) {
        return new LdapConnection(clientSocket, userSessionFactory, searchManager, txManager, propFindPropertyBuilder);
    }

    @Override
    public synchronized void start() {
        try {
            log.info("Created server, binding to address. bind address: " + bindAddress + " port: " + port);
            bind();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        log.info("Starting the LDAP server thread");
        super.start();
    }

    /**
     * Bind server socket on defined port.
     *
     * @throws DavMailException unable to create server socket
     */
    public void bind() throws Exception {
        ServerSocketFactory serverSocketFactory;
        if (keystoreFile == null || keystoreFile.length() == 0 || nosslFlag) {
            serverSocketFactory = ServerSocketFactory.getDefault();
        } else {
            FileInputStream keyStoreInputStream = null;
            try {
                keyStoreInputStream = new FileInputStream(keystoreFile);
                // keystore for keys and certificates
                // keystore and private keys should be password protected...
                KeyStore keystore = KeyStore.getInstance(keystoreType);
                keystore.load(keyStoreInputStream, keystorePass.toCharArray());

                // KeyManagerFactory to create key managers
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

                // initialize KMF to work with keystore
                kmf.init(keystore, keystorePass.toCharArray());

                // SSLContext is environment for implementing JSSE...
                // create ServerSocketFactory
                SSLContext sslContext = SSLContext.getInstance("SSLv3");

                // initialize sslContext to work with key managers
                sslContext.init(kmf.getKeyManagers(), null, null);

                // create ServerSocketFactory from sslContext
                serverSocketFactory = sslContext.getServerSocketFactory();
            } catch (IOException ex) {
                throw new Exception(ex);
            } catch (GeneralSecurityException ex) {
                throw new Exception(ex);
            } finally {
                if (keyStoreInputStream != null) {
                    try {
                        keyStoreInputStream.close();
                    } catch (IOException exc) {
                        log.error("exception closing stream", exc);
                    }
                }
            }
        }
        try {
            // create the server socket
            if (bindAddress == null || bindAddress.length() == 0) {
                log.info("Starting LDAP server on all interfaces and port: " + port);
                serverSocket = serverSocketFactory.createServerSocket(port);
            } else {
                log.info("Starting LDAP server on interface: " + bindAddress + " and port: " + port);
                serverSocket = serverSocketFactory.createServerSocket(port, 0, Inet4Address.getByName(bindAddress));
            }
        } catch (IOException e) {
            throw new Exception(e);
        }
    }

    /**
     * The body of the server thread. Loop forever, listening for and accepting
     * connections from clients. For each connection, create a Connection object
     * to handle communication through the new Socket.
     */
    @Override
    public void run() {
        Socket clientSocket = null;
        LdapConnection connection = null;
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                System.out.println("Waiting for connection...");
                clientSocket = serverSocket.accept();
                System.out.println("Accepted socket from: " + clientSocket.getRemoteSocketAddress());
                // set default timeout to 5 minutes
                clientSocket.setSoTimeout(300000);
                log.info("CONNECTION_FROM" + clientSocket.getInetAddress() + port);
                // only accept localhost connections for security reasons
                if (allowRemote || clientSocket.getInetAddress().isLoopbackAddress()) {
                    connection = createConnectionHandler(clientSocket);
                    connection.start();
                } else {
                    clientSocket.close();
                    log.warn("external connection refused");
                }
            }
        } catch (IOException e) {
            // do not warn if exception on socket close (gateway restart)
            if (!serverSocket.isClosed()) {
                log.warn("exception", e);
            }
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                log.warn("exception", e);
            }
            if (connection != null) {
                connection.close();
            }
        }
        System.out.println("LDAP Server has exited");
    }

    /**
     * Close server socket
     */
    public void close() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.error("LOG_EXCEPTION_CLOSING_SERVER_SOCKET", e);
        }
    }

    /**
     * Server socket TCP port
     *
     * @return port
     */
    public int getPort() {
        return port;
    }

    public String getKeystorePass() {
        return keystorePass;
    }

    public void setKeystorePass(String keystorePass) {
        this.keystorePass = keystorePass;
    }

    public String getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
    }

    public File getKeystoreFile() {
        return keystoreFile;
    }

    public void setKeystoreFile(File keystoreFile) {
        this.keystoreFile = keystoreFile;
    }

    public boolean isAllowRemote() {
        return allowRemote;
    }

    public void setAllowRemote(boolean allowRemote) {
        this.allowRemote = allowRemote;
    }
}