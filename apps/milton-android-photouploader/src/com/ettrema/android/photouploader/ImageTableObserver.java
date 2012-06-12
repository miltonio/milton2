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

import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import java.util.concurrent.ExecutorService;

/**
 * Class to observe changes to images table
 * 
 * @author Jan Peter Hooiveld
 */
public class ImageTableObserver extends ContentObserver {

    private static final String TAG = "ImageTableObserver";

    /**
     * Main application
     */
    private MiltonPhotoUploader application;
    /**
     *  Queue that handles image uploads
     */
    private ExecutorService queue;

    /**
     * Constructor
     *
     * @param handler Handler for this class
     * @param application Main application
     * @param queue Queue that handles image uploads
     */
    public ImageTableObserver( Handler handler, MiltonPhotoUploader application, ExecutorService queue ) {
        super( handler );

        this.application = application;
        this.queue = queue;
    }

    /**
     * This function is fired when a change occurs on the image table
     *
     * @param selfChange
     */
    @Override
    public void onChange( boolean selfChange ) {
        Log.i(TAG, "onChange");

        // get latest image id
        ImageLatest latestImage = new ImageLatest( application );
        int imageId = latestImage.getId();

        // if id is -1 it means no record was found or it was a update/delete instead of insert
        if( imageId == -1 ) {
            return;
        }

        // get image item
        ImageItem item = latestImage.getLatestItem();

        // if no image item returned abort
        if( item == null ) {
            return;
        }

        // get user preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( application.getBaseContext() );

        // check if we can connect to internet
        if( !CheckInternet.getInstance().canConnect( application.getBaseContext(), prefs ) ) {
            Log.v(TAG, "cant connect to internet");
            return;
        }

        // check if albums from preferences does not exist
        if( !albumExists( ) ) {
            Log.i(TAG, "onChange");
            return;
        }

        // add item to queue
        Log.i(TAG, "add to queue: " + item.imagePath);
        queue.execute( new MiltonPutUploader( application.getBaseContext(), queue, item, application.getConfig(), 0 ) );
    }


    /**
     * Check if Picasa album stored in user preferences exists
     *
     * @param auth Google authentication string
     * @param prefs User preferences
     * @return If album exists or not
     */
    private boolean albumExists( ) {
        return new AlbumExists( application.getConfig() ).hasAlbum();
    }
}
