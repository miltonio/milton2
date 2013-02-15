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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamUtils {

    private static Logger log = LoggerFactory.getLogger(StreamUtils.class);

    private StreamUtils() {
    }

    private static void skip(InputStream in, Long start) {
        try {
            in.skip(start);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static long readTo(File inFile, OutputStream out, boolean closeOut) throws ReadingException, WritingException {
        try {
            final FileInputStream in = new FileInputStream(inFile);
            try {
                return readTo(in, out);
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    log.error("exception closing output stream", ex);
                }
                if (closeOut) {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        log.error("exception closing outputstream", ex);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static long readTo(InputStream in, File outFile, boolean closeIn) throws ReadingException, WritingException {
        try {
            final FileOutputStream out = new FileOutputStream(outFile);
            try {
                return readTo(in, out);
            } finally {
                try {
                    out.close();
                } catch (IOException ex) {
                    log.error("exception closing output stream", ex);
                }
                if (closeIn) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        log.error("exception closing inputstream", ex);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Copies data from in to out and DOES NOT close streams
     *
     * @param in
     * @param out
     * @return
     * @throws com.bradmcevoy.io.ReadingException
     * @throws com.bradmcevoy.io.WritingException
     */
    public static long readTo(InputStream in, OutputStream out) throws ReadingException, WritingException {
        return readTo(in, out, false, false, null, null);
    }

    /**
     * Reads bytes from the input and writes them, completely, to the output.
     * Closes both streams when finished depending on closeIn and closeOyt
     *
     * @param in
     * @param out
     * @param closeIn
     * @param closeOut
     * @return - number of bytes written
     * @throws com.bradmcevoy.io.ReadingException
     * @throws com.bradmcevoy.io.WritingException
     */
    public static long readTo(InputStream in, OutputStream out, boolean closeIn, boolean closeOut) throws ReadingException, WritingException {
        return readTo(in, out, closeIn, closeOut, null, null);
    }

    public static long readTo(InputStream in, OutputStream out, boolean closeIn, boolean closeOut, Long start, Long finish) throws ReadingException, WritingException {
        long cnt = 0;
        if (start != null) {
            skip(in, start);
            cnt = start;
        }

        byte[] buf = new byte[1024];
        int s;
        try {
            try {
                s = in.read(buf);
            } catch (IOException ex) {
                throw new ReadingException(ex);
            } catch (NullPointerException e) {
                log.debug("nullpointer exception reading input stream. it happens for sun.nio.ch.ChannelInputStream.read(ChannelInputStream.java:48)");
                return cnt;
            }
            long numBytes = 0;
            while (s > 0) {
                try {
                    numBytes += s;
                    cnt += s;
                    out.write(buf, 0, s);
                    if (cnt > 10000) {
                        out.flush();
                        cnt = 0;
                    }
                } catch (IOException ex) {
                    //throw new WritingException(ex);
                    log.error("writing exectpion");
                }
                try {
                    s = in.read(buf);
                } catch (IOException ex) {
                    throw new ReadingException(ex);
                }
            }
            try {
                out.flush();
            } catch (IOException ex) {
                throw new WritingException("Write exception at byte: " + numBytes, ex); 
            }
            return numBytes;
        } finally {
            if (closeIn) {
                close(in);
            }
            if (closeOut) {
                close(out);
            }
        }
    }

    public static void close(OutputStream out) {
        if (out == null) {
            return;
        }
        try {
            out.close();
        } catch (IOException ex) {
            log.warn("exception closing output stream", ex);
        }
    }

    public static void close(InputStream in) {
        if (in == null) {
            return;
        }
        try {
            in.close();
        } catch (IOException ex) {
            log.warn("exception closing inputstream", ex);
        }
    }
}
