/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
