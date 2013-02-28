/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
