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

/** proxyconn is glue between two Sockets wrapped in dataconn, to pss traffic between them. **/
class ProxyConn implements Runnable {

    public boolean debug = false;
    protected Thread t;
    protected DataConn c1,  c2;

    public ProxyConn() {
    }

    public ProxyConn(DataConn _c1, DataConn _c2) {
        c1 = _c2;
        c2 = _c2;
    }

    public ProxyConn(Socket s1, Socket s2) {
        c1 = new DataConn(s1);
        c2 = new DataConn(s2);
    }

    public ProxyConn(Socket s1, String thost, int tport) {
        c1 = new DataConn(s1);
        c2 = new DataConn(thost, tport);
    }

    public void go() {
        c1.debug = debug;
        c1.debugname = "c1";
        c2.debug = debug;
        c2.debugname = "c2";
        t = new Thread(this);
        t.start();
    }

    protected void log(boolean fromc1, byte[] d) {
    }

    public void run() {
        while (dopolling()) {
        }
    }

    /* dopolling is called in run() loop. return false to close proxy connection **/
    protected boolean dopolling() {
        try {
            byte[] d;
            d = c1.read();
            if (d != null) {
                log(true, d);
                c2.write(d);
            }
            d = c2.read();
            if (d != null) {
                log(false, d);
                c1.write(d);
            }
            if (c1.error || c2.error) {
                return false;
            }
        } catch (Throwable T) {
            exception(T);
            return false;
        }
        return true;
    }

    protected void exception(Throwable T) {
        System.err.println("proxyconn ERR" + T.getMessage());
    }
}
