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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author brad
 */
public class StringSplitUtils {
    //~ Static fields/initializers =====================================================================================

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    //~ Constructors ===================================================================================================
    private StringSplitUtils() {
    }

    //~ Methods ========================================================================================================
    /**
     * Splits a <code>String</code> at the first instance of the delimiter.<p>Does not include the delimiter in
     * the response.</p>
     *
     * @param toSplit   the string to split
     * @param delimiter to split the string up with
     * @return a two element array with index 0 being before the delimiter, and index 1 being after the delimiter
     *         (neither element includes the delimiter)
     * @throws IllegalArgumentException if an argument was invalid
     */
    public static String[] split( String toSplit, String delimiter ) {

        if( delimiter.length() != 1 ) {
            throw new IllegalArgumentException(
                "Delimiter can only be one character in length" );
        }

        int offset = toSplit.indexOf( delimiter );

        if( offset < 0 ) {
            return null;
        }

        String beforeDelimiter = toSplit.substring( 0, offset );
        String afterDelimiter = toSplit.substring( offset + 1 );

        return new String[]{beforeDelimiter, afterDelimiter};
    }

    /**
     * Takes an array of <code>String</code>s, and for each element removes any instances of
     * <code>removeCharacter</code>, and splits the element based on the <code>delimiter</code>. A <code>Map</code> is
     * then generated, with the left of the delimiter providing the key, and the right of the delimiter providing the
     * value.<p>Will trim both the key and value before adding to the <code>Map</code>.</p>
     *
     * @param array            the array to process
     * @param delimiter        to split each element using (typically the equals symbol)
     * @param removeCharacters one or more characters to remove from each element prior to attempting the split
     *                         operation (typically the quotation mark symbol) or <code>null</code> if no removal should occur
     * @return a <code>Map</code> representing the array contents, or <code>null</code> if the array to process was
     *         null or empty
     */
    public static Map splitEachArrayElementAndCreateMap( String[] array,
        String delimiter, String removeCharacters ) {
        if( ( array == null ) || ( array.length == 0 ) ) {
            return null;
        }

        Map map = new HashMap();

        for( int i = 0; i < array.length; i++ ) {
            String postRemove;

            if( removeCharacters == null ) {
                postRemove = array[i];
            } else {
                postRemove = replace( array[i], removeCharacters, "" );
            }

            String[] splitThisArrayElement = split( postRemove,
                delimiter );

            if( splitThisArrayElement == null ) {
                continue;
            }

            map.put( splitThisArrayElement[0].trim(),
                splitThisArrayElement[1].trim() );
        }

        return map;
    }

    /**
     *Replacealloccurencesofasubstringwithinastringwith
     *anotherstring.
     *@paraminStringStringtoexamine
     *@paramoldPatternStringtoreplace
     *@paramnewPatternStringtoinsert
     *@returnaStringwiththereplacements
     */
    public static String replace( String inString, String oldPattern, String newPattern ) {
        if( inString == null ) {
            return null;
        }
        if( oldPattern == null || newPattern == null ) {
            return inString;
        }
        StringBuilder sbuf = new StringBuilder();

        int pos = 0;//ourpositionintheoldstring
        int index = inString.indexOf( oldPattern );

        int patLen = oldPattern.length();
        while( index >= 0 ) {
            sbuf.append( inString.substring( pos, index ) );
            sbuf.append( newPattern );
            pos = index + patLen;
            index = inString.indexOf( oldPattern, pos );
        }
        sbuf.append( inString.substring( pos ) );

        return sbuf.toString();
    }

    public static String substringBeforeLast( String str,
        String separator ) {
        if( str == null || separator == null || str.length() == 0
            || separator.length() == 0 ) {
            return str;
        }
        int pos = str.lastIndexOf( separator );
        if( pos == -1 ) {
            return str;
        }
        return str.substring( 0, pos );
    }

    public static String substringAfterLast( String str, String separator ) {
        if( str == null || str.length() == 0 ) {
            return str;
        }
        if( separator == null || separator.length() == 0 ) {
            return "";
        }
        int pos = str.lastIndexOf( separator );
        if( pos == -1 || pos == ( str.length() - separator.length() ) ) {
            return "";
        }
        return str.substring( pos + separator.length() );
    }

    /**
     * Splits a given string on the given separator character, skips the contents of quoted substrings
     * when looking for separators.
     * Introduced for use in DigestProcessingFilter (see SEC-506).
     * <p/>
     * This was copied and modified from commons-lang StringUtils
     */
    public static String[] splitIgnoringQuotes( String str,
        char separatorChar ) {
        if( str == null ) {
            return null;
        }

        int len = str.length();

        if( len == 0 ) {
            return EMPTY_STRING_ARRAY;
        }

        List list = new ArrayList();
        int i = 0;
        int start = 0;
        boolean match = false;

        while( i < len ) {
            if( str.charAt( i ) == '"' ) {
                i++;
                while( i < len ) {
                    if( str.charAt( i ) == '"' ) {
                        i++;
                        break;
                    }
                    i++;
                }
                match = true;
                continue;
            }
            if( str.charAt( i ) == separatorChar ) {
                if( match ) {
                    list.add( str.substring( start, i ) );
                    match = false;
                }
                start = ++i;
                continue;
            }
            match = true;
            i++;
        }
        if( match ) {
            list.add( str.substring( start, i ) );
        }

        return (String[]) list.toArray( new String[list.size()] );
    }
}
