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

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images.Media;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MiltonPhotoUploader extends Activity {

    /**
     * Observer that listens to changes on image table
     */
    private ImageTableObserver camera;
    /**
     * Highest image id in database
     */
    private int maxId;
    /**
     * Image item queue
     */
    private ExecutorService queue = Executors.newSingleThreadExecutor();
    /**
     * Menu item to send application to background
     */
    private static final int MENU_BACK = 1;
    /**
     * Menu item for user preferences
     */
    private static final int MENU_PREFS = 2;
    /**
     * Menu item for user preferences
     */
    private static final int MENU_LICENSE = 3;
    /**
     * Menu item to exit application
     */
    private static final int MENU_EXIT = 4;

    private Config config;

    /**
     * Main appplication constructor
     *
     * @param parent
     */
    @Override
    public void onCreate( Bundle bundle ) {
        // call parent
        super.onCreate( bundle );

        config = new Config( this );

        // set main layout screen
        setContentView( R.layout.main );

        // get user preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( getBaseContext() );

        // check if application notification is on
        if( prefs.getString( "notification", "" ).contains( "enabled" ) ) {
            ApplicationNotification.getInstance().enable( getBaseContext() );
        }

        // store highest image id from database in application
        setMaxIdFromDatabase();

        // register camera observer
        camera = new ImageTableObserver( new Handler(), this, queue );
        getContentResolver().registerContentObserver( Media.EXTERNAL_CONTENT_URI, true, camera );
    }

    public Config getConfig() {
        return config;
    }

    

    /**
     * Override parent function so back button won't stop application
     * but instead we send it to background
     *
     * @param keyCode Button that was pressed
     * @param event Even information
     * @return Parent function
     */
    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event ) {
        if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 ) {
            moveTaskToBack( true );
            return true;
        }

        // call parent function for other keys and events
        return super.onKeyDown( keyCode, event );
    }

    /**
     * Create application menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        // call parent
        super.onCreateOptionsMenu( menu );

        // add menu items
        menu.add( 0, MENU_BACK, Menu.NONE, "Send to background" ).setIcon( android.R.drawable.ic_menu_set_as );
        menu.add( 1, MENU_PREFS, Menu.NONE, "Preferences" ).setIcon( android.R.drawable.ic_menu_preferences );
        menu.add( 2, MENU_LICENSE, Menu.NONE, "License" ).setIcon( android.R.drawable.ic_menu_view );
        menu.add( 3, MENU_EXIT, Menu.NONE, "Exit" ).setIcon( android.R.drawable.ic_menu_close_clear_cancel );

        // return
        return true;
    }

    /**
     * Code to execute when menu item is selected
     *
     * @param item Menu item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch( item.getItemId() ) {
            // move task to background
            case MENU_BACK:
                moveTaskToBack( true );
                break;
            // start user preferences screen
            case MENU_PREFS:
                startActivity( new Intent( this, EditPreferences.class ) );
                break;
            // show license
            case MENU_LICENSE:
                Utils.textDialog( this, "License Information", getString( R.string.license ) );
                break;
            // kill the queue, all running notifications and exit application
            // usual way is to use finish() but uploads will keep running otherwise
            case MENU_EXIT:
                queue.shutdownNow();
                ( (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE ) ).cancelAll();
                System.exit( 0 );
                break;
        }

        // return parent
        return ( super.onOptionsItemSelected( item ) );
    }

    /**
     * Store highest image id from image table
     */
    private void setMaxIdFromDatabase() {
        String columns[] = new String[]{Media._ID, Media.DISPLAY_NAME, Media.MINI_THUMB_MAGIC};
        Cursor cursor = managedQuery( Media.EXTERNAL_CONTENT_URI, columns, null, null, Media._ID + " DESC" );
        maxId = cursor.moveToFirst() ? cursor.getInt( cursor.getColumnIndex( Media._ID ) ) : -1;
    }

    /**
     * Set highest image id
     *
     * @param maxId New value for maxId
     */
    public void setMaxId( int maxId ) {
        this.maxId = maxId;
    }

    /**
     * Get highest image id
     *
     * @return Highest id
     */
    public int getMaxId() {
        return maxId;
    }
}
