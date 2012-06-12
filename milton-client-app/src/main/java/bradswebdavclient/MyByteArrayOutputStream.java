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

package bradswebdavclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author mcevoyb
 */
public class MyByteArrayOutputStream extends ByteArrayOutputStream {
    public MyByteArrayOutputStream() {
    }
    
    public void read(InputStream in) {
        try {
            int cnt;
            byte[] buffer = new byte[1024];
            while ((cnt = in.read(buffer)) > 0) {
                write(buffer,0,cnt);
            }
            close();
            in.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
