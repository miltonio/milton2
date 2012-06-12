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
		FileInputStream in = null;
		try {
			in = new FileInputStream(inFile);
			return readTo(in, out);
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
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
	}

	public static long readTo(InputStream in, File outFile, boolean closeIn) throws ReadingException, WritingException {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outFile);
			return readTo(in, out);
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
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
	 * Reads bytes from the input and writes them, completely, to the output. Closes both streams when
	 * finished depending on closeIn and closeOyt
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
					throw new WritingException(ex);
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
				throw new WritingException(ex);
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
