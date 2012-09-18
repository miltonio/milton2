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

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.impl.FtpHandler;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class MiltonListenerFactory extends ListenerFactory{

    private static final Logger log = LoggerFactory.getLogger( MiltonListenerFactory.class );
     

    private final FtpHandler ftpHandler;

    public MiltonListenerFactory( FtpHandler ftpHandler ) {
        this.ftpHandler = ftpHandler;
    }

    

    @Override
    public Listener createListener() {
    	try{
    		InetAddress.getByName(this.getServerAddress());
    	}catch(UnknownHostException e){
    		throw new FtpServerConfigurationException("Unknown host",e);
    	}
        log.debug( "Creating milton listener");
        return new MiltonListener(getServerAddress(), getPort(), isImplicitSsl(), getSslConfiguration(),
                getDataConnectionConfiguration(), getIdleTimeout(), getBlockedAddresses(),
                getBlockedSubnets(), ftpHandler);
    }
    
}
