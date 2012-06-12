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

package io.milton.httpclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 *
 * @author mcevoyb
 */
public class MyByteArrayOutputStream extends ByteArrayOutputStream {

    public MyByteArrayOutputStream() {
        super();
    }

    public void readFully( InputStream in ) throws IOException {
        byte[] arr = new byte[1024];
        int i = in.read( arr );
        while( i >= 0 ) {
            write( arr, 0, i );
            i = in.read( arr );
        }
    }

    public InputStream asIn() {
        ByteArrayInputStream in = new ByteArrayInputStream( this.toByteArray() );
        return in;
    }
}
