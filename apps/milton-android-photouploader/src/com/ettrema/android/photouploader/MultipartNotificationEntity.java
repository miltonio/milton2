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

import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.entity.ByteArrayEntity;

/**
 * Class to override ByteArrayEntity for HttpClient to write multipart related
 * content and to progress upload monitoring for notification when uploading
 * a file to Picasa
 *
 * @author Jan Peter Hooiveld
 */
public class MultipartNotificationEntity extends ByteArrayEntity {

    private static final String TAG = "MultipartNotificationEntity";

    /**
     * Upload notification
     */
    private UploadNotification notification;

    /**
     * Constructor
     *
     * @param multipart Multipart class that creates the content
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public MultipartNotificationEntity( Multipart multipart, UploadNotification notification ) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        // call parent to set content
        super( multipart.getContent() );

        // add notification
        this.notification = notification;

        // set content type
        setContentType( "multipart/related; boundary=\"" + multipart.getBoundary() + "\"" );
    }

    /**
     * Write content to outputstream of HttpClient
     *
     * @param outstream Outputstreaam of the HttpClient
     * @throws IOException
     */
    @Override
    public void writeTo( final OutputStream outstream ) throws IOException {
        Log.i( TAG, "writeTo: length: " + content.length);
        // check if we have an outputstrean
        if( outstream == null ) {
            throw new IllegalArgumentException( "Output stream may not be null" );
        }

        // create file input stream
        InputStream instream = new ByteArrayInputStream( this.content );

        try {
            // create vars
            byte[] tmp = new byte[4096];
            int total = this.content.length;
            int progress = 0;
            int increment = 10;
            int l;
            int percent;

            // read file and write to http output stream
            while( ( l = instream.read( tmp ) ) != -1 ) {
                // check progress
                progress = progress + l;
                percent = Math.round( ( (float) progress / (float) total ) * 100 );

                // if percent exceeds increment update status notification
                // and adjust increment
                if( percent > increment ) {
                    increment += 10;
                    Log.i( TAG, "progress: " + increment);
                    notification.update( progress );
                }

                // write to output stream
                outstream.write( tmp, 0, l );
            }

            // flush output stream
            outstream.flush();
            Log.i( TAG, "finished sending");
        } finally {
            // close input stream
            instream.close();
        }
    }
}
