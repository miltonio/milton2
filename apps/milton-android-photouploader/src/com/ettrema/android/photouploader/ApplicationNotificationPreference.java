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
import android.preference.ListPreference;
import android.util.AttributeSet;

/**
 * Edit application notification preference
 *
 * @author Hooiveld
 */
public class ApplicationNotificationPreference extends ListPreference {

    /**
     * Application context
     */
    private Context context;

    /**
     * Constructor
     *
     * @param context Application context
     * @param attrs Attributes
     */
    public ApplicationNotificationPreference( Context context, AttributeSet attrs ) {
        super( context, attrs );

        this.context = context;
    }

    /**
     * Override parent setValue to enable or disable application notification
     * after user has selected prefered choice
     *
     * @param value Notification value
     */
    @Override
    public void setValue( String value ) {
        // enable or disable application notification
        if( value.contains( "enabled" ) ) {
            ApplicationNotification.getInstance().enable( context );
        } else {
            ApplicationNotification.getInstance().disable();
        }

        super.setValue( value );
    }
}
