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

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images.Media;

/**
 * Class to get latest image from database
 *
 * @author Jan Peter Hooiveld
 */
public class ImageLatest {

    /**
     * Main application
     */
    private MiltonPhotoUploader application;
    /**
     * Latest image id in database
     */
    private int latestId;

    /**
     *
     * @param application Main application
     */
    public ImageLatest( MiltonPhotoUploader application ) {
        this.application = application;
    }

    /**
     * Get highest id from database
     *
     * @return highest image id in database or -1 if conditions fail
     */
    public int getId() {
        String[] columns = new String[]{Media._ID, Media.ORIENTATION};
        Cursor cursor = application.managedQuery( Media.EXTERNAL_CONTENT_URI, columns, null, null, Media._ID + " DESC" );

        // check if table has any rows at all
        if( !cursor.moveToFirst() ) {
            return -1;
        }

        // get latest id from db and stored id in application
        latestId = cursor.getInt( cursor.getColumnIndex( Media._ID ) );
        int maxId = application.getMaxId();

        // if id from db is equal or lower to stored id it means user changed or
        // deleted somewhere in table so store the new highest id and return
        if( latestId <= maxId ) {
            application.setMaxId( latestId );
            return -1;
        }

        // If orientation is null it means new image is not a photo but we will
        // store highest id and return
        String orientation = cursor.getString( cursor.getColumnIndex( Media.ORIENTATION ) );

        if( orientation == null ) {
            application.setMaxId( latestId );
            return -1;
        }

        // store latest id in application
        application.setMaxId( latestId );

        // return latest id
        return latestId;
    }

    /**
     * The problem is that the observer is invoked multiple times when a photo is
     * taken with the camera. It could be that on the first time the observer is
     * triggered the record is already in the database but the file isn't
     * completely written to the sdcard. That's why we need to check if the field
     * MINI_THUMB_MAGIC is not null. If this field is not null it means the photo
     * was written to sdcard completely and we can start our upload.
     *
     * @return Image queue item
     */
    public ImageItem getLatestItem() {
        // set vars
        ImageItem item = null;
        String columns[] = new String[]{Media._ID, Media.DATA, Media.DISPLAY_NAME, Media.MIME_TYPE, Media.SIZE, Media.MINI_THUMB_MAGIC};

        // loop until break
        while( true ) {
            // get latest image from table
            Uri image = ContentUris.withAppendedId( Media.EXTERNAL_CONTENT_URI, latestId );
            Cursor cursor = application.managedQuery( image, columns, null, null, null );

            // check if cursus has rows, if not break and exit loop
            if( cursor.moveToFirst() ) {
                // get thumbnail field
                String imageThumb = cursor.getString( cursor.getColumnIndex( Media.MINI_THUMB_MAGIC ) );

                // if thumbnail field is not null it means image is written to sdcard
                // create new image item and break loop otherwise restart loop to check again
                if( imageThumb != null ) {
                    item = new ImageItem();
                    item.imageId = cursor.getInt( cursor.getColumnIndex( Media._ID ) );
                    item.imagePath = cursor.getString( cursor.getColumnIndex( Media.DATA ) );
                    item.imageName = cursor.getString( cursor.getColumnIndex( Media.DISPLAY_NAME ) );
                    item.imageType = cursor.getString( cursor.getColumnIndex( Media.MIME_TYPE ) );
                    item.imageSize = cursor.getInt( cursor.getColumnIndex( Media.SIZE ) );

                    break;
                }
            } else {
                break;
            }
        }

        return item;
    }
}
