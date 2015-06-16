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
