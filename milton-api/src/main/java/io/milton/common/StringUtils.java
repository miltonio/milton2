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

package io.milton.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author brad
 */
public abstract class StringUtils {

    public static String[] delimitedListToStringArray( String str, String delimiter ) {
        if( str == null ) {
            return new String[0];
        }
        if( delimiter == null ) {
            return new String[]{str};
        }
        List result = new ArrayList();
        if( "".equals( delimiter ) ) {
            for( int i = 0; i < str.length(); i++ ) {
                result.add( str.substring( i, i + 1 ) );
            }
        } else {
            int pos = 0;
            int delPos = 0;
            while( ( delPos = str.indexOf( delimiter, pos ) ) != -1 ) {
                result.add( str.substring( pos, delPos ) );
                pos = delPos + delimiter.length();
            }
            if( str.length() > 0 && pos <= str.length() ) {
                // Add rest of String, but not in case of empty input.
                result.add( str.substring( pos ) );
            }
        }
        return toStringArray( result );
    }

    public static String[] toStringArray( Collection collection ) {
        if( collection == null ) {
            return null;
        }
        return (String[]) collection.toArray( new String[collection.size()] );
    }

	/**
	 * 
	 * true iff the given string contains any of the given strings
	 * 
	 * @param ua
	 * @param browserIds
	 * @return 
	 */
	public static boolean contains(String ua, String[] browserIds) {	
		for(String s : browserIds ) {
			if( ua.contains(s)) {
				return true;
			}
		}
		return false;
	}
}
