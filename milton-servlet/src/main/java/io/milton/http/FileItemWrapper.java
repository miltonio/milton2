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


package io.milton.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.fileupload.FileItem;

public class FileItemWrapper implements io.milton.http.FileItem{

    final org.apache.commons.fileupload.FileItem wrapped;

    final String name;
    
    /**
     * strip path information provided by IE
     * 
     * @param s
     * @return
     */
    public static String fixIEFileName(String s) {
        if (s.contains("\\")) {
            int pos = s.lastIndexOf('\\');
            s = s.substring(pos+1);
        }
        return s;
    }
    
    public FileItemWrapper(FileItem wrapped) {
        this.wrapped = wrapped;
        name = fixIEFileName(wrapped.getName());
    }        
    
    public String getContentType() {
        return wrapped.getContentType();
    }

    public String getFieldName() {
        return wrapped.getFieldName();
    }

    public InputStream getInputStream() {
        try {
            return wrapped.getInputStream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public OutputStream getOutputStream() {
        try {
            return wrapped.getOutputStream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public String getName() {
        return name;
    }

    public long getSize() {
        return wrapped.getSize();
    }
}
