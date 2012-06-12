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

/**
 * This file is part of Picasa Photo Uploader.
 *
 * Picasa Photo Uploader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Picasa Photo Uploader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Picasa Photo Uploader. If not, see <http://www.gnu.org/licenses/>.
 */
package com.ettrema.android.photouploader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;

/**
 * Class to create MultiPart related body for use in uploading to Picasa
 *
 * @author Jan Peter Hooiveld
 */
public class Multipart {

    /**
     * The name of the file being uploaded
     */
    private final String name;
    /**
     * Defines the boundary used in the body for each part
     */
    private String boundary;
    /**
     * Buffer that holds the content
     */
    private ByteArrayBuffer buffer = new ByteArrayBuffer( 0 );
    /**
     * Line ending
     */
    private static final String CR_LF = "\r\n";
    /**
     * Dashes for the boundary
     */
    private static final String DASHES = "--";

    /**
     * Constructor
     *
     * @param description Description of the body contents
     * @param boundary Defines the boundary used in the body for each part
     * @throws UnsupportedEncodingException
     */
    public Multipart( String name, String boundary ) throws UnsupportedEncodingException {
        this.name = name;
        this.boundary = CR_LF + DASHES + boundary;

    }

    /**
     * Returns the boundary
     *
     * @return Boundary
     */
    public String getBoundary() {
        return boundary.substring( 4 );
    }

    /**
     * Adds new file to the body
     *
     * @param file File object
     * @param contentType File content type
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void addPart( File file, String contentType ) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        // merge start into content

        String disp = "form-data; name=\"name\"; filename=\"" + name + "\"";
        mergePart( boundary + CR_LF );
        mergePart( addHeader( "Content-Disposition", disp ) );
        mergePart( addHeader( "Content-Type", contentType ) );
        mergePart( CR_LF );

        // create vars to reas file
        InputStream is = new FileInputStream( file );
        byte[] bytes = new byte[(int) file.length()];
        int offset = 0;
        int numRead = 0;

        // reaf file into byte array
        while( offset < bytes.length && ( numRead = is.read( bytes, offset, bytes.length - offset ) ) >= 0 ) {
            offset += numRead;
        }

        // errror checking
        if( offset < bytes.length ) {
            throw new IOException( "Could not completely read file " + file.getName() );
        }

        // close input stream
        is.close();

        // merge the file into content
        mergePart( bytes );
    }

    /**
     * Accumulate body content and return it
     *
     * @return Body content
     * @throws UnsupportedEncodingException
     */
    public byte[] getContent() throws UnsupportedEncodingException {
        // create new byte array buffer and get start and end as byte arrays
        ByteArrayBuffer endBuffer = new ByteArrayBuffer( 0 );
        byte[] start = buffer.toByteArray();
        byte[] end = ( boundary + DASHES ).getBytes( HTTP.DEFAULT_CONTENT_CHARSET );

        // append the start and end to new byte array buffer
        endBuffer.append( start, 0, start.length );
        endBuffer.append( end, 0, end.length );

        // return buffer as content
        return endBuffer.toByteArray();
    }

    /**
     * Creates start of a part
     *
     * @param contentType Content type of the part
     * @return Start of a part
     */
    private String addHeader( String headerName, String contentType ) {
        return headerName + ": " + contentType + CR_LF;
    }

    /**
     * Merge content data from part
     *
     * @param input Part data to merge into content
     * @throws UnsupportedEncodingException
     */
    private void mergePart( Object input ) throws UnsupportedEncodingException {
        byte[] bytes = null;

        if( input instanceof String ) {
            bytes = ( (String) input ).getBytes( HTTP.DEFAULT_CONTENT_CHARSET );
        } else if( input instanceof byte[] ) {
            bytes = (byte[]) input;
        }

        if( bytes != null ) {
            buffer.append( bytes, 0, bytes.length );
        }
    }
}
