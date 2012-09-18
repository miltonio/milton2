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

