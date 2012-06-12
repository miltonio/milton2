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

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

/**
 * Check if internet connection is available
 *
 * @author Jan Peter Hooiveld
 */
public class CheckInternet {

    /**
     * Singleton that refers to this class
     */
    private static CheckInternet instance;
    /**
     * Constant for Wi-FI setting from user preferences
     */
    private static final String CON_WIFI = "wifi";

    /**
     * Constructor
     */
    private CheckInternet() {
    }

    /**
     * Singleton function
     *
     * @return Instance of this class
     */
    public static CheckInternet getInstance() {
        if( instance == null ) {
            instance = new CheckInternet();
        }

        return instance;
    }

    /**
     * Block cloning since we use singleton
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Can we connect to the internet or not, checks if device has connection
     * and if wifi is on if user has set that in the connection preference
     *
     * @param context Application context
     * @param prefs User preferences
     * @return Can connect to internet or not
     */
    @SuppressWarnings( "static-access" )
    public boolean canConnect( Context context, SharedPreferences prefs ) {
        // get wifi manager
        WifiManager wifi = (WifiManager) context.getSystemService( Context.WIFI_SERVICE );

        // if wifi is disabled and the user preference only allows wifi abort
        if( wifi.getWifiState() != wifi.WIFI_STATE_ENABLED && prefs.getString( "connection", "" ).equals( CON_WIFI ) ) {
            return false;
        }

        // get connectivity manager
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );

        // check if we have active network and that network is connected, if not abort
        if( manager.getActiveNetworkInfo() == null || !manager.getActiveNetworkInfo().isConnected() ) {
            return false;
        }

        // we can use the internet connection
        return true;
    }
}
