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

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/** proxylogconn is a logging version of proxyconn.
Stores log files in "log' subdirectory **/
public class ProxyLogConn extends ProxyConn {

    protected PrintWriter lout;

    public ProxyLogConn(Socket clientSocket, String targetHost, int taretPort) {
        c1 = new DataConn(clientSocket);
        c2 = new DataConn(targetHost, taretPort);

        try {
            InetAddress rhost = clientSocket.getInetAddress();
            String rhostname = rhost.getHostName();
            lout = new PrintWriter(System.out);
            lout.println("// CLIENT: " + rhostname);
            lout.println("// TARGET: " + targetHost);
        } catch (Throwable T) {
            System.err.println("proxylogserver ERR: " + T.getMessage());
        }
    }

    @Override
    public void run() {
        super.run();
        if (lout != null) {
            lout.close();
        }
    }

    @Override
    protected void finalize() {
        try {
            if (lout != null) {
                lout.close();
            }
        } catch (Throwable T) {
        }
    }

    @Override
    protected void exception(Throwable T) {
        try {
            System.err.println("EXCEPTION: " + T.getMessage());
            lout.println("EXCEPTION: " + T.getMessage());
        } catch (Throwable T1) {
        }
    }

    @Override
    protected void log(boolean fromc1, byte[] d) {
        try {
            if (fromc1) {
                lout.print("c(\"");
            } else {
                lout.print("s(\"");
            }
            lout.print(printableBytes(d));
            lout.println("\");");
            lout.flush();
        } catch (Throwable T) {
            exception(T);
        }

    }

    static String printableBytes(byte[] bytes) {
        if (bytes == null) {
            return "*NONE*";
        }
        StringBuilder s = new StringBuilder();
        int i;
        for (i = 0; i < bytes.length;) {
            int b = bytes[i];
            if (b < 0) {
                b = 256 + b;  // byte is signed type!
            }
            s.append((char) b);
//            if (b < ' ' || b > 0x7f) {
//                int d1 = (int) (b >> 6) & 7;
//                b = b & 0x3f;
//                int d2 = (int) (b >> 3) & 7;
//                int d3 = (int) b & 7;
//                s.append("\\" + d1);
//                s.append(d2);
//                s.append(d3);
//            } else if ('\\' == (char) b) {
//                s.append("\\\\");
//            } else {
//                s.append((char) b);
//            }
            i++;
            if (0 == (i - (400 * (i / 400)))) {
                s.append("\n");
            }

//            if (0 == (i - (40 * (i / 40)))) {
//                s.append("\"+\n   \"");
//            }
        }
        String ss = s.toString();
        System.out.println(ss);
        return ss;
    }
}

