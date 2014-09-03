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

import java.io.IOException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletContext;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class MiltonFtplet implements Ftplet{

    private static final Logger log = LoggerFactory.getLogger( MiltonFtplet.class );

    public void init( FtpletContext ftpletContext ) throws FtpException {
        log.debug( "init");
    }

    public void destroy() {
        log.debug( "destroy");
    }

    public FtpletResult beforeCommand( FtpSession ftpSession, FtpRequest ftpRequest ) throws FtpException, IOException {
        log.debug( "beforeCommand");
        return FtpletResult.DEFAULT;
    }

    public FtpletResult afterCommand( FtpSession ftpSession, FtpRequest ftpRequest, FtpReply ftpReply ) throws FtpException, IOException {
        log.debug( "afterCommand");
        return FtpletResult.DEFAULT;
    }

    public FtpletResult onConnect( FtpSession ftpSession ) throws FtpException, IOException {
        log.debug( "onConnect");
        return FtpletResult.DEFAULT;
    }

    public FtpletResult onDisconnect( FtpSession ftpSession ) throws FtpException, IOException {
        log.debug( "onDisconnect");
        return FtpletResult.DEFAULT;
    }

}
