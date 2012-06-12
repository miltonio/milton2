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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Class to maintain upload notification
 *
 * @author Jan Peter Hooiveld
 */
public class UploadNotification extends Notification {

    /**
     * Unique notificaion id
     */
    private int id;
    /**
     * Filesize used as maximum value for progressbar
     */
    private int max;
    /**
     * Current upload progress of file
     */
    private int progress = 0;
    /**
     * Name of file being uploaded
     */
    private String filename;
    /**
     * Manager that deals with notifications
     */
    private NotificationManager manager;

    /**
     * Constructor, creates new status notification for file being uploaded
     *
     * @param context Application context
     * @param id Unique notification id
     * @param max Filesize used as maximum value for progressbar
     * @param filename Name of file being uploaded
     */
    @SuppressWarnings( "static-access" )
    public UploadNotification( Context context, int id, int max, String filename ) {
        // call parent
        super();

        // set class vars
        this.id = id;
        this.max = max;
        this.filename = filename;

        try {
            // set manager, etra info, the custom view and intent
            manager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );
            icon = android.R.drawable.stat_sys_upload;
            tickerText = "Uploading: " + filename;
            flags |= super.FLAG_ONGOING_EVENT;
            contentView = new RemoteViews( context.getPackageName(), R.layout.upload );
            Intent intent = new Intent( context, this.getClass() );
            contentIntent = PendingIntent.getActivity( context, 0, intent, 0 );

            // set fields in the custom view
            contentView.setImageViewResource( R.id.uploadImage, android.R.drawable.stat_sys_upload );
            contentView.setTextViewText( R.id.uploadText, "Uploading: " + filename );
            contentView.setProgressBar( R.id.uploadBar, max, progress, false );

            // show notification to user
            manager.notify( id, this );
        } catch( Exception e ) {
        }
    }

    /**
     * Update status notificaion with the new progress
     *
     * @param progress Progress of file upload
     */
    public void update( int progress ) {
        // update file upload progress
        this.progress += progress;

        try {
            // set fields in the custom view
            icon = android.R.drawable.stat_sys_upload;
            contentView.setTextViewText( R.id.uploadText, "Uploading: " + filename );
            contentView.setProgressBar( R.id.uploadBar, max, this.progress, false );

            // change notification for user
            manager.notify( id, this );
        } catch( Exception e ) {
        }
    }

    /**
     * Update status notification that file upload has finished
     */
    public void finished() {
        try {
            // change flags and icon
            flags = ~Notification.FLAG_ONGOING_EVENT;
            flags += Notification.FLAG_AUTO_CANCEL;
            icon = android.R.drawable.stat_sys_upload_done;

            // set fields in the custom view
            contentView.setImageViewResource( R.id.uploadImage, android.R.drawable.stat_sys_upload_done );
            contentView.setTextViewText( R.id.uploadText, "Uploaded: " + filename );
            contentView.setProgressBar( R.id.uploadBar, max, max, false );

            // change notification for user
            manager.notify( id, this );
        } catch( Exception e ) {
        }
    }

    /**
     * Update status notification that file upload has failed
     */
    public void failed() {
        try {
            // change flags and icon
            flags = ~Notification.FLAG_ONGOING_EVENT;
            flags += Notification.FLAG_AUTO_CANCEL;
            icon = android.R.drawable.stat_sys_warning;

            // set fields in the custom view
            contentView.setImageViewResource( R.id.uploadImage, android.R.drawable.stat_sys_warning );
            contentView.setTextViewText( R.id.uploadText, "Upload failed: " + filename );
            contentView.setProgressBar( R.id.uploadBar, max, 0, false );

            // change notification for user
            manager.notify( id, this );
        } catch( Exception e ) {
        }
    }

    /**
     * Remove status notification
     */
    public void remove() {
        try {
            // remove notification for user
            manager.cancel( id );
        } catch( Exception e ) {
        }
    }
}
