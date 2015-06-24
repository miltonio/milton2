
/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.milton.simpleton;

import io.milton.common.FileUtils;
import io.milton.common.ReadingException;
import io.milton.common.StreamUtils;
import io.milton.common.WritingException;
import io.milton.http.FileItem;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.simpleframework.http.Part;

/**
 *
 * @author bradm (zfc1502)
 */
public class SimpleFileItem implements FileItem{

    public final String name;
    public final String contentType;
    public final String fileName;

    private long size;
    private java.io.File f;
    private FileOutputStream out;


    public SimpleFileItem(String name, String contentType, String fileName) {
        try {
            this.name = name;
            this.contentType = contentType;
            this.fileName = fileName;
            f = File.createTempFile("upload", name);
            out = new FileOutputStream(f);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getFieldName() {
        return name;
    }

    @Override
    public InputStream getInputStream() {
        try {
            return new FileInputStream(f);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getName() {
        return fileName;
    }

    @Override
    public long getSize() {
        return f.length();
    }

    @Override
    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void addPart(Part part) {
        try {
            StreamUtils.readTo(part.getInputStream(), out);
        } catch (ReadingException ex) {
            throw new RuntimeException(ex);
        } catch (WritingException ex) {
            throw new RuntimeException(ex);
        } catch( Exception ex ) {
            throw new RuntimeException(ex);
        }
    }

    void finishedReadingRequest() {
        FileUtils.close(out);
    }
}
