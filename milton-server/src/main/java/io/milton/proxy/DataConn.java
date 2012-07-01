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

// proxylogserver.java - sample TCP proxy server with logging capability
// By Rowland http://rowland.blcss.com
import java.io.*;
import java.net.*;

/** dataconn encapsulates a Socket with some ease of use features **/
class DataConn {

    public boolean debug = false;
    public String debugname = null;
    public boolean error = false;
    protected Socket sconn;
    protected InputStream is;
    protected OutputStream os;

    public DataConn() {
    }

    public DataConn(String szhost, int port) {
        try {
            connect(new Socket(InetAddress.getByName(szhost), port));
        } catch (Throwable T) {
            System.err.println("new dataconn ERR: " + T.getMessage());
            error = true;
        }
    }

    public DataConn(InetAddress rhost, int port) {
        try {
            connect(new Socket(rhost, port));
        } catch (Throwable T) {
            System.err.println("new dataconn ERR: " + T.getMessage());
            error = true;
        }
    }

    public DataConn(Socket _s) {
        connect(_s);
    }

    public void setDebug(boolean _debug) {
        debug = _debug;
    }

    protected void finalize() {
        close();
    }

    protected void connect(Socket _s) {
        sconn = _s;
        connect();
    }

    protected void connect() {
        error = false;
        try {
            is = sconn.getInputStream();
            os = sconn.getOutputStream();
        } catch (Throwable T) {
            exception(T);
            error = true;
        }
    }

    protected void close() {
        if (sconn == null) {
            return;
        }
        try {
            os = null;
            is = null;
            sconn.close();
            sconn = null;
        } catch (Throwable E) {
            System.err.println("dataconn.close ERR: " + E.getMessage());
            error = true;
        }
    }

    protected final void write(String d) {
        write(d.getBytes());
    }

    protected final void write(byte[] d) {
        if (error) {
            return;
        }
        try {
            os.write(d);
            os.flush();
            debuglog(false, d);
            log(false, d);
        } catch (Throwable T) {
            exception(T);
            error = true;
        }
    }

    protected final byte[] read() {
        if (error) {
            return null;
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException IE) {
            return null;
        }
        try {
            int iavail = is.available();
            if (iavail > 0) {
                byte[] d = new byte[iavail];
                is.read(d);
                debuglog(true, d);
                log(true, d);
                return d;
            }
        } catch (Throwable T) {
            exception(T);
            error = true;
        }
        return null;
    }
    // exception and log handling...

    protected final void debuglog(boolean isread, byte[] d) {
        if (!debug) {
            return;
        }
        if (debugname != null) {
            System.err.print(debugname + " ");
        }
        if (isread) {
            System.err.print("R:");
        } else {
            System.err.print("W:");
        }
        System.err.println(" " + d.length + " bytes");
    }

    protected void log(boolean isread, byte[] d) {
        // override in derived class
    }

    protected void exception(Throwable T) {
        String m = "EXCEPTION: " + T.getMessage();
        System.err.println(m);
        if (!(T instanceof SocketException)) {
            T.printStackTrace();
        }
    }
}

