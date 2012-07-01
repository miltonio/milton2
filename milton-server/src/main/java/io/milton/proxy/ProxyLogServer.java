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

    public static void main(String args[]) {
        String targetHost = args[0];
        Integer targetPort = new Integer(args[1]);
        Integer listenPort = new Integer(args[2]);
        ProxyLogServer us = new ProxyLogServer(targetHost, targetPort, listenPort);
        us.go();
    }
}

