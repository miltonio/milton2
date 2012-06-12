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

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Class to check if Picasa album exists for a user.
 * it could be that the user has deleted the album
 * while the album is still stored in the user 
 * preferences of this application
 *
 * @author Jan Peter Hooiveld
 */
public class AlbumExists {

    private final Config config;

    /**
     * Constructor
     *
     * @param auth Google authentication string
     * @param email User google e-mail
     * @param album User Picasa album
     */
    public AlbumExists( Config config ) {
        this.config = config;
    }

    /**
     * Check if the user album exists in Picasa
     *
     * @return User album exists or not
     */
    public boolean hasAlbum() {
//        try {
//            // set http items
//            String albumUrl = "http://picasaweb.google.com/data/feed/api/user/" + email + "/albumid/" + album;
//            HttpURLConnection con = (HttpURLConnection) new URL( albumUrl ).openConnection();
//
//            // request method, timeout and headers
//            con.setRequestMethod( "GET" );
//            con.setReadTimeout( 15000 );
//            con.setRequestProperty( "Authorization", "GoogleLogin auth=" + auth );
//            con.setRequestProperty( "GData-Version", "2" );
//
//            // execute connection
//            con.connect();
//
//            // if return code is greater than 200 it means album was not found
//            if( con.getResponseCode() > 200 ) {
//                return false;
//            }
//        } catch( Exception e ) {
//            return false;
//        }
//
//        // we found the Picasa album
//        return true;
        return true;
    }
}
