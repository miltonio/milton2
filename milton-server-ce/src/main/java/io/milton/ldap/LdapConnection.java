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
import io.milton.property.PropertySource;
import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerDecoder;
import io.milton.common.LogUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.callback.*;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslServer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle a caldav connection.
 *
 * This is the server part of a LDAP client to server connection. This will
 * locate information in some user repository (such as a milton carddav
 * implementation) and format the results as LDAP messages.
 *
 */
public class LdapConnection extends Thread {

    private static final Logger log = LoggerFactory.getLogger(LdapConnection.class);
    /**
     * Sasl server for DIGEST-MD5 authentication
     */
    protected SaslServer saslServer;
    /**
     * raw connection inputStream
     */
    protected BufferedInputStream is;
    private final UserFactory userFactory;
    private final LdapPropertyMapper propertyMapper;
    private final LdapResponseHandler responseHandler;
    private final LdapParser ldapParser;
    private final Socket client;
    private final SearchManager searchManager;
    private final LdapTransactionManager txManager;
    private LdapPrincipal user;
    private LineReaderInputStream in;
    private final OutputStream os;
    // user name and password initialized through connection
    private String userName;
    private String password;
    protected static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * Initialize the streams and start the thread.
     *
     * @param clientSocket LDAP client socket
     */
    public LdapConnection(Socket clientSocket, UserFactory userSessionFactory, SearchManager searchManager, LdapTransactionManager txManager, PropFindPropertyBuilder propFindPropertyBuilder) {
        super(LdapConnection.class.getSimpleName() + '-' + clientSocket.getPort());
        this.searchManager = searchManager;
        this.client = clientSocket;
        this.txManager = txManager;
        setDaemon(true);
        this.userFactory = userSessionFactory;
        this.propertyMapper = new LdapPropertyMapper(propFindPropertyBuilder);
        try {
            is = new BufferedInputStream(client.getInputStream());
            os = new BufferedOutputStream(client.getOutputStream());
        } catch (IOException e) {
            close();
            throw new RuntimeException(e);
        }
        responseHandler = new LdapResponseHandler(client, os);
        ldapParser = new LdapParser(propertyMapper, responseHandler, userFactory);
        System.out.println("Created LDAP Connection handler");
    }

    @Override
    public void run() {
        byte[] inbuf = new byte[2048];   // Buffer for reading incoming bytes
        int bytesread;  // Number of bytes in inbuf
        int bytesleft;  // Number of bytes that need to read for completing resp
        int br;         // Temp; number of bytes read from stream
        int offset;     // Offset of where to store bytes in inbuf
        boolean eos;    // End of stream

        try {
            while (true) {
                offset = 0;

                // check that it is the beginning of a sequence
                bytesread = is.read(inbuf, offset, 1);
                if (bytesread < 0) {
                    break; // EOF
                }
                System.out.println("read bytes: " + bytesread);

                if (inbuf[offset++] != (Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR)) {
                    continue;
                }

                // get length of sequence
                bytesread = is.read(inbuf, offset, 1);
                if (bytesread < 0) {
                    break; // EOF
                }
                int seqlen = inbuf[offset++]; // Length of ASN sequence

                // if high bit is on, length is encoded in the
                // subsequent length bytes and the number of length bytes
                // is equal to & 0x80 (i.e. length byte with high bit off).
                if ((seqlen & 0x80) == 0x80) {
                    int seqlenlen = seqlen & 0x7f;  // number of length bytes

                    bytesread = 0;
                    eos = false;

                    // Read all length bytes
                    while (bytesread < seqlenlen) {
                        br = is.read(inbuf, offset + bytesread,
                                seqlenlen - bytesread);
                        if (br < 0) {
                            eos = true;
                            break; // EOF
                        }
                        bytesread += br;
                    }

                    // end-of-stream reached before length bytes are read
                    if (eos) {
                        break;  // EOF
                    }

                    // Add contents of length bytes to determine length
                    seqlen = 0;
                    for (int i = 0; i < seqlenlen; i++) {
                        seqlen = (seqlen << 8) + (inbuf[offset + i] & 0xff);
                    }
                    offset += bytesread;
                }

                // read in seqlen bytes
                bytesleft = seqlen;
                if ((offset + bytesleft) > inbuf.length) {
                    byte[] nbuf = new byte[offset + bytesleft];
                    System.arraycopy(inbuf, 0, nbuf, 0, offset);
                    inbuf = nbuf;
                }
                while (bytesleft > 0) {
                    bytesread = is.read(inbuf, offset, bytesleft);
                    if (bytesread < 0) {
                        break; // EOF
                    }
                    offset += bytesread;
                    bytesleft -= bytesread;
                }


                handleRequest(inbuf, offset);
            }

        } catch (SocketException e) {
            log.debug("LOG_CONNECTION_CLOSED");
        } catch (SocketTimeoutException e) {
            log.debug("LOG_CLOSE_CONNECTION_ON_TIMEOUT");
        } catch (Exception e) {
            log.error("err", e);
            try {
                responseHandler.sendErr(0, Ldap.LDAP_REP_BIND, e);
            } catch (IOException e2) {
                log.warn("LOG_EXCEPTION_SENDING_ERROR_TO_CLIENT", e2);
            }
        } finally {
            searchManager.cancelAllSearches(this);
            close();
        }
    }

    protected void handleRequest(final byte[] inbuf, final int offset) throws IOException {
        try {
            txManager.tx(new Runnable() {

                @Override
                public void run() {
                    try {
                        _handleRequest(inbuf, offset);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        } catch (Throwable e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    protected void _handleRequest(byte[] inbuf, int offset) throws IOException {
        //dumpBer(inbuf, offset);
        BerDecoder reqBer = new BerDecoder(inbuf, 0, offset);
        int currentMessageId = 0;
        try {
            reqBer.parseSeq(null);
            currentMessageId = reqBer.parseInt();
            int requestOperation = reqBer.peekByte();

            if (requestOperation == Ldap.LDAP_REQ_BIND) {
                reqBer.parseSeq(null);
                responseHandler.setVersion(reqBer.parseInt());
                userName = reqBer.parseString(responseHandler.isLdapV3());
				log.info("Bind user name: " + userName);
                if (reqBer.peekByte() == (Ber.ASN_CONTEXT | Ber.ASN_CONSTRUCTOR | 3)) {
                    // SASL authentication
                    reqBer.parseSeq(null);
                    // Get mechanism, usually DIGEST-MD5
                    String mechanism = reqBer.parseString(responseHandler.isLdapV3());

                    byte[] serverResponse;
                    CallbackHandler callbackHandler = new CallbackHandler() {

                        @Override
                        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                            // look for username in callbacks
                            for (Callback callback : callbacks) {
                                if (callback instanceof NameCallback) {
                                    userName = ((NameCallback) callback).getDefaultName();
                                    // get password from session pool
                                    password = userFactory.getUserPassword(userName);
                                }
                            }
                            // handle other callbacks
                            for (Callback callback : callbacks) {
                                if (callback instanceof AuthorizeCallback) {
                                    ((AuthorizeCallback) callback).setAuthorized(true);
                                } else if (callback instanceof PasswordCallback) {
                                    if (password != null) {
                                        ((PasswordCallback) callback).setPassword(password.toCharArray());
                                    }
                                }
                            }
                        }
                    };
                    int status;
                    if (reqBer.bytesLeft() > 0 && saslServer != null) {
                        byte[] clientResponse = reqBer.parseOctetString(Ber.ASN_OCTET_STR, null);
                        serverResponse = saslServer.evaluateResponse(clientResponse);
                        status = Ldap.LDAP_SUCCESS;

                        LogUtils.debug(log, "LOG_LDAP_REQ_BIND_USER", currentMessageId, userName);
                        user = userFactory.getUser(userName, password);
						if( user != null ) {
							LogUtils.debug(log, "LOG_LDAP_REQ_BIND_SUCCESS");
						} else {
							LogUtils.debug(log, "LOG_LDAP_REQ_BIND", "No user! " + userName);
						}

                    } else {
                        Map<String, String> properties = new HashMap<String, String>();
                        properties.put("javax.security.sasl.qop", "auth,auth-int");
                        saslServer = Sasl.createSaslServer(mechanism, "ldap", client.getLocalAddress().getHostAddress(), properties, callbackHandler);
                        serverResponse = saslServer.evaluateResponse(EMPTY_BYTE_ARRAY);
                        status = Ldap.LDAP_SASL_BIND_IN_PROGRESS;
                    }
                    responseHandler.sendBindResponse(currentMessageId, status, serverResponse);

                } else {
                    password = reqBer.parseStringWithTag(Ber.ASN_CONTEXT, responseHandler.isLdapV3(), null);

                    if (userName.length() > 0 && password.length() > 0) {
                        log.debug("LOG_LDAP_REQ_BIND_USER", currentMessageId, userName);
                        try {
                            user = userFactory.getUser(userName, password);
                            LogUtils.debug(log, "LOG_LDAP_REQ_BIND_SUCCESS");
                            responseHandler.sendClient(currentMessageId, Ldap.LDAP_REP_BIND, Ldap.LDAP_SUCCESS, "");
                        } catch (IOException e) {
                            LogUtils.debug(log, "LOG_LDAP_REQ_BIND_INVALID_CREDENTIALS");
                            responseHandler.sendClient(currentMessageId, Ldap.LDAP_REP_BIND, Ldap.LDAP_INVALID_CREDENTIALS, "");
                        }
                    } else {
                        LogUtils.debug(log, "LOG_LDAP_REQ_BIND_ANONYMOUS", currentMessageId);
                        // anonymous bind
                        responseHandler.sendClient(currentMessageId, Ldap.LDAP_REP_BIND, Ldap.LDAP_SUCCESS, "");
                    }
                }

            } else if (requestOperation == Ldap.LDAP_REQ_UNBIND) {
                log.debug("LOG_LDAP_REQ_UNBIND", currentMessageId);
                if (user != null) {
                    user = null;
                }
            } else if (requestOperation == Ldap.LDAP_REQ_SEARCH) {
                reqBer.parseSeq(null);
                String dn = reqBer.parseString(responseHandler.isLdapV3());
				log.info("Parsed DN: " + dn);
                int scope = reqBer.parseEnumeration();
                /*
                 * int derefAliases =
                 */
                reqBer.parseEnumeration();
                int sizeLimit = reqBer.parseInt();
                if (sizeLimit > 100 || sizeLimit == 0) {
                    sizeLimit = 100;
                }
                int timelimit = reqBer.parseInt();
                /*
                 * boolean typesOnly =
                 */
                reqBer.parseBoolean();
                LdapFilter ldapFilter = ldapParser.parseFilter(reqBer, user, userName);
                Set<String> returningAttributes = ldapParser.parseReturningAttributes(reqBer);
                SearchRunnable searchRunnable = new SearchRunnable(userFactory, propertyMapper, currentMessageId, dn, scope, sizeLimit, timelimit, ldapFilter, returningAttributes, responseHandler, user, searchManager);
                if (Ldap.BASE_CONTEXT.equalsIgnoreCase(dn) || Ldap.OD_USER_CONTEXT.equalsIgnoreCase(dn) || Ldap.OD_USER_CONTEXT_LION.equalsIgnoreCase(dn)) {
                    // launch search in a separate thread
                    searchManager.beginAsyncSearch(this, currentMessageId, searchRunnable);
                } else {
                    // no need to create a separate thread, just run
                    searchManager.search(this, searchRunnable);
                }

            } else if (requestOperation == Ldap.LDAP_REQ_ABANDON) {
                searchManager.abandonSearch(this, currentMessageId, reqBer);
            } else {
                LogUtils.debug(log, "LOG_LDAP_UNSUPPORTED_OPERATION", requestOperation);
                responseHandler.sendClient(currentMessageId, Ldap.LDAP_REP_RESULT, Ldap.LDAP_OTHER, "Unsupported operation");
            }
        } catch (IOException e) {
            responseHandler.dumpBer(inbuf, offset);
            try {
                responseHandler.sendErr(currentMessageId, Ldap.LDAP_REP_RESULT, e);
            } catch (IOException e2) {
                log.debug("LOG_EXCEPTION_SENDING_ERROR_TO_CLIENT", e2);
            }
            throw e;
        }
    }

    /**
     * Close client connection, streams and Exchange session .
     */
    public final void close() {
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(os);
        try {
            client.close();
        } catch (IOException e2) {
            log.warn("LOG_EXCEPTION_CLOSING_CLIENT_SOCKET", e2);
        }
    }
}
