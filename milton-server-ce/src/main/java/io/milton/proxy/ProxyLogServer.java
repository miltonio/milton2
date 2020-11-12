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

package io.milton.proxy;

import java.net.Socket;

/** proxylogserver is a logging version of proxyserver.
Stores log files in "log' subdirectory **/
public class ProxyLogServer extends ProxyServer {

    public ProxyLogServer() {
    }

    public ProxyLogServer(String targetHost, int targetPort, int listenPort) {
        super(targetHost, targetPort, listenPort);
    }

    @Override
    protected void gotconn(Socket sconn) throws Exception {
        ProxyLogConn pc = new ProxyLogConn(sconn, getTargetHost(), getTargetPort());
        pc.go();
    }

    public static void main(String[] args) {
        String targetHost = args[0];
        Integer targetPort = new Integer(args[1]);
        Integer listenPort = new Integer(args[2]);
        ProxyLogServer us = new ProxyLogServer(targetHost, targetPort, listenPort);
        us.go();
    }
}

