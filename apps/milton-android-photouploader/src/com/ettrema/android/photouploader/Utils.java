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

package com.ettrema.android.photouploader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class with support functions
 * 
 * @author Hooiveld
 */
public final class Utils {

    public static void textDialog( Context context, String title, String text ) {
        new AlertDialog.Builder( context ).setTitle( title ).setMessage( text ).setNeutralButton( "OK",
            new DialogInterface.OnClickListener() {

                @Override
                public void onClick( DialogInterface dialog, int which ) {
                    dialog.cancel();
                }
            } ).show();
    }

    public static byte[] toByteArray( File file ) throws IOException {
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

        return bytes;

    }
}
