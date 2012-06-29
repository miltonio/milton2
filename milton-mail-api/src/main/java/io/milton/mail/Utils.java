package io.milton.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public class Utils {
    public static void close(OutputStream out) {
        if( out == null ) return ;
        try {
            out.close();
        } catch (IOException ex) {
            
        }
    }

    public static void close(InputStream in) {
        if( in == null ) return ;
        try {
            in.close();
        } catch (IOException ex) {

        }
    }

    public static String parseContentId(String s) {
        int pos = s.indexOf('<');
        if( pos >= 0 ) s = s.substring(pos+1);
        pos = s.lastIndexOf('>');
        if( pos > 0 ) s = s.substring(0, pos);
        return s;
    }
}
