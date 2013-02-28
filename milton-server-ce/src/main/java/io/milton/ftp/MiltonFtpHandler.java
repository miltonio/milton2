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

package io.milton.ftp;

import java.net.SocketAddress;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpHandler;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.listener.Listener;
import org.apache.mina.core.session.IdleStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps DefaultFtpHandler, adding support for filters
 *
 * @author brad
 */
public class MiltonFtpHandler implements FtpHandler {
    private static final Logger log = LoggerFactory.getLogger( MiltonFtpFile.class );
    private final FtpHandler wrapped;
    private final FtpActionListener actionListener;

    public MiltonFtpHandler( FtpHandler wrapped,FtpActionListener actionListener ) {
        this.wrapped = wrapped;
        this.actionListener = actionListener;
    }

    public void init( FtpServerContext arg0, Listener arg1 ) {
        wrapped.init( arg0, arg1 );
    }

    public void sessionCreated( FtpIoSession arg0 ) throws Exception {
        wrapped.sessionCreated( arg0 );
    }

    public void sessionOpened( FtpIoSession arg0 ) throws Exception {
        wrapped.sessionOpened( arg0 );
    }

    public void sessionClosed( FtpIoSession arg0 ) throws Exception {
        wrapped.sessionClosed( arg0 );
    }

    public void sessionIdle( FtpIoSession arg0, IdleStatus arg1 ) throws Exception {
        wrapped.sessionIdle( arg0, arg1 );
    }

    public void exceptionCaught( FtpIoSession arg0, Throwable arg1 ) throws Exception {
        wrapped.exceptionCaught( arg0, arg1 );
    }

    public void messageReceived( final FtpIoSession session, final FtpRequest request ) throws Exception {
        SocketAddress sa = session.getServiceAddress();
        log.debug( "message received: " + sa.toString());
        if( actionListener != null ) {
            actionListener.onAction( new Runnable() {

                public void run() {
                    try {
                        wrapped.messageReceived( session, request );
                    } catch( Exception ex ) {
                        throw new RuntimeException( ex );
                    }
                }
            });
        } else {
            wrapped.messageReceived( session, request );
        }
    }

    public void messageSent( FtpIoSession ioSession, FtpReply ftpReply ) throws Exception {
        wrapped.messageSent( ioSession, ftpReply );
    }
}
