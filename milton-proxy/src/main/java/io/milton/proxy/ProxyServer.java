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

import com.ettrema.common.Service;
import java.net.ServerSocket;
import java.net.Socket;

/** proxyserver listens on given lport, forwards traffic to tport on thost **/
public class ProxyServer implements Runnable, Service {
    private boolean debug = false;
    private int targetPort;
    private int listenPort;
    private String targetHost;
    protected Thread thread;
    private boolean running;

    public ProxyServer() {
    }


    public ProxyServer(String targetHost, int targetPort, int listenPort) {
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.listenPort = listenPort;
    }

    public void go() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void run() {
        System.out.println("starting listening on: " + listenPort);
        try {
            ServerSocket ss = new ServerSocket(listenPort);
            if (debug) {
                System.err.println("proxyserver: " + listenPort + " listening");
            }
            while (running) {
                Socket sconn = ss.accept();
                if (debug) {
                    System.err.print(" gotConn: " + listenPort + " ");
                }
                gotconn(sconn);
            }
        } catch (Throwable e) {
            if (debug) {
                System.err.println("proxyserver: " + listenPort + " " + e.toString());
            }
            e.printStackTrace();
        }
    }

    protected void gotconn(Socket sconn) throws Exception {
        ProxyConn pc = new ProxyConn(sconn, targetHost, targetPort);
        pc.go();
    }

    public static void main(String args[]) {
        String targetHost = args[0];
        Integer targetPort = new Integer(args[1]);
        Integer listenPort = new Integer(args[2]);
        ProxyServer us = new ProxyServer(targetHost, targetPort, listenPort);
        us.go();
    }

    public void start() {
        go();
    }

    public void stop() {
        thread.interrupt();
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @return the targetPort
     */
    public int getTargetPort() {
        return targetPort;
    }

    /**
     * @param targetPort the targetPort to set
     */
    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    /**
     * @return the listenPort
     */
    public int getListenPort() {
        return listenPort;
    }

    /**
     * @param listenPort the listenPort to set
     */
    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    /**
     * @return the targetHost
     */
    public String getTargetHost() {
        return targetHost;
    }

    /**
     * @param targetHost the targetHost to set
     */
    public void setTargetHost(String targetHost) {
        this.targetHost = targetHost;
    }
    

}

