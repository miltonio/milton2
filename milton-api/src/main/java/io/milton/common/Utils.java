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
package io.milton.common;

import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;

import io.milton.http.webdav.Dest;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class Utils {
    
    public static final Charset UTF8 = Charset.forName("UTF-8");
    
    private final static char[] hexDigits = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static Resource findChild(Resource parent, Path path) throws NotAuthorizedException, BadRequestException {
        return _findChild(parent, path.getParts(), 0);
    }

    /**
     * does percentage decoding on a path portion of a url
     *
     * E.g. /foo > /foo /with%20space -> /with space
     *
     * @param href
     */
    public static String decodePath(String href) {
        // For IPv6
        href = href.replace("[", "%5B").replace("]", "%5D");

        // Seems that some client apps send spaces.. maybe..
        href = href.replace(" ", "%20");
        try {
            if (href.startsWith("/")) {
                URI uri = new URI("http://anything.com" + href);
                return uri.getPath();
            } else {
                URI uri = new URI("http://anything.com/" + href);
                String s = uri.getPath();
                return s.substring(1);
            }
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Resource _findChild(Resource parent, String[] arr, int i) throws NotAuthorizedException, BadRequestException {
        if (parent instanceof CollectionResource) {
            CollectionResource col = (CollectionResource) parent;
            String childName = arr[i];

            Resource child = col.child(childName);
            if (child == null) {
                return null;
            } else {
                if (i < arr.length - 1) {
                    return _findChild(child, arr, i + 1);
                } else {
                    return child;
                }
            }
        } else {
            return null;
        }
    }

    public static Date now() {
        return new Date();
    }

    public static Date addSeconds(Date dt, long seconds) {
        return addSeconds(dt, (int) seconds);
    }

    public static Date addSeconds(Date dt, int seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.add(Calendar.SECOND, seconds);
        return cal.getTime();
    }

    public static String getProtocol(String url) {
        String protocol = url.substring(0, url.indexOf(":"));
        return protocol;
    }

    public static String escapeXml(String s) {
        s = s.replaceAll("\"", "&quot;");
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("'", "&apos;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
//        s = s.replaceAll("�", "ae");
        return s;
    }

    /**
     * this is a modified verion of java.net.URI.encode(s)
     *
     * the java.net version only encodes characters over \u0080, but this
     * version also applies encoding to characters below char 48
     *
     * this method should be applied only to parts of a URL, not the whole URL
     * as forward slashes, semi-colons etc will be encoded
     *
     * by "part of url" i mean the bits between slashes
     *
     * @param s
     */
    public static String percentEncode(String s) {
        //s = _percentEncode( s ); // the original method, from java.net
        s = encodeURL(s, "UTF-8");
        return s;
    }

    /**
     * This method has been provided by Andr� Kunert - looks a bit better then
     * my shabby implementation! BM
     *
     * @param str
     * @param charset
     * @return
     */
    public static String encodeURL(String str, String charset) {
        StringBuilder buf = new StringBuilder();
        byte[] daten;
        try {
            daten = charset == null ? str.getBytes() : str.getBytes(charset);
        } catch (Exception e) {
            daten = str.getBytes();
        }
        int length = daten.length;
        for (int i = 0; i < length; i++) {
            char c = (char) (daten[i] & 0xFF);
            switch (c) {
                case '-':
                case '_':
                case '.':
                case '*':
//                case ':':
//                case '/':
                    buf.append(c);
                    break;
                default:
                    if (('0' <= c && c <= '9') || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')) {
                        buf.append(c);
                    } else {
                        buf.append('%');
                        buf.append(hexDigits[(c >> 4) & 0x0F]);
                        buf.append(hexDigits[c & 0x0F]);
                    }
            }
        }
        return buf.toString();
    }

    private static String _percentEncode(String s) {
        int n = s.length();
        if (n == 0) {
            return s;
        }

        String ns = normalize(s);
        ByteBuffer bb = null;
        bb = Charset.forName("UTF-8").encode(CharBuffer.wrap(ns));

        StringBuilder sb = new StringBuilder();
        while (bb.hasRemaining()) {
            int b = bb.get() & 0xff;
            // **ONLY** unreserved characters can be added unencoded
            if (isUnReserved(b)) {
                sb.append((char) b);
            } else {
                appendEscape(sb, (byte) b);
            }
        }
        return sb.toString();
    }

    /**
     * Range 1 - dec 65 - 90 A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
     *
     * Range 2 - dec 97 - 122 a b c d e f g h i j k l m n o p q r s t u v w x y
     * z
     *
     * Range 3 - dec 48 - 57 0 1 2 3 4 5 6 7 8 9
     *
     * 45 46 95 126 - . _ ~
     *
     * @param b
     * @return
     */
    private static boolean isUnReserved(int b) {
        return inRange(b, 65, 90)
                || inRange(b, 97, 122)
                || inRange(b, 48, 57)
                || inList(b, 45, 46, 95, 126);
    }

    private static boolean inRange(int b, int lower, int upper) {
        return b >= lower && b <= upper;
    }

    private static boolean inList(int b, int... nums) {
        for (int i : nums) {
            if (b == i) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSquareBracket(int b) {
        return b == 0x5B || b == 0x5D;
    }

    private static void appendEscape(StringBuilder sb, byte b) {
        sb.append('%');
        sb.append(hexDigits[(b >> 4) & 0x0f]);
        sb.append(hexDigits[(b) & 0x0f]);
    }

    public static Date mostRecent(Date... dates) {
        if (dates == null || dates.length == 0) {
            return null;
        }
        Date recent = dates[0];
        for (Date dt : dates) {
            if (dt.getTime() > recent.getTime()) {
                recent = dt;
            }
        }
        return recent;
    }

    /**
     * java.text.Normalizer is only available for jdk 1.6. Since it isnt really
     * required and we don't want to annoy our 1.5 colleagues, this is commented
     * out.
     *
     * It isnt really needed because URLs still get consistently encoded and
     * decoded without it. Its just that you might get different results on
     * different platforms
     *
     * @param s
     * @return
     */
    private static String normalize(String s) {
        //return Normalizer.normalize(s, Normalizer.Form.NFC);
        return s;
    }

    /**
     * Convert the list of strings to a comma separated string
     *
     * @param list
     * @return - a comma seperated list of values
     */
    public static String toCsv(Collection<String> list) {
        if( list == null || list.isEmpty()) {
            return null;
        }
        String res = "";
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            res += it.next();
            if (it.hasNext()) {
                res += ", ";
            }
        }
        return res;
    }

    public static String stripServer(String href) {
        if (href.startsWith("http")) {
            return href.substring(href.indexOf("/", 8));
        } else {
            return href;
        }
    }

    /**
     * Used for parsing uploaded file names. MS web browsers tend to transmit
     * the complete path for an uploaded file, but we generally only want to
     * know the last part of the path.
     *
     * TODO: move this into milton
     *
     * @param s
     * @return
     */
    public static String truncateFileName(String agent, String s) {
        if (agent == null) {
            return s;
        } else {
            if (agent.contains("MSIE")) {
                if (s.contains("\\")) {
                    int pos = s.lastIndexOf("\\");
                    return s.substring(pos + 1);
                } else {
                    return s;
                }
            } else {
                return s;
            }
        }
    }

    /**
     * If n is > max, returns max. Otherwise n
     *
     * @param n
     * @param max
     * @return
     */
    public static long withMax(long n, long max) {
        if (n > max) {
            return max;
        } else {
            return n;
        }
    }

    /**
     * Add a slash if not present
     *
     * @param parentHref
     * @return
     */
    public static String suffixSlash(String parentHref) {
        if (parentHref == null) {
            return null;
        } else if (parentHref.endsWith("/")) {
            return parentHref;
        } else {
            return parentHref + "/";
        }
    }

    public static Dest getDecodedDestination(String destinationHeader) {
        String sDest = destinationHeader;
        URI destUri = URI.create(sDest);
        sDest = destUri.getPath();
        Dest dest = new Dest(destUri.getHost(), sDest);
        return dest;
    }   
}
