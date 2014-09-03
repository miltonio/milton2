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

package io.milton.servlet.upload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import javax.activation.DataSource;
import org.apache.commons.fileupload.FileItem;

public class FileItemExt implements DataSource, FileItem {
    
    private static final long serialVersionUID = 1L;
    
    private final FileItem item;
    
    public FileItemExt(FileItem item) {
        this.item = item;
    }

    public InputStream getInputStream() throws IOException {
        return item.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return item.getOutputStream();
    }

    public String getContentType() {
        return item.getContentType();
    }

    public String getName() {
        return item.getName();
    }

    public boolean isInMemory() {
        return item.isInMemory();
    }

    public long getSize() {
        return item.getSize();
    }

    public byte[] get() {
        return item.get();
    }

    public String getString(String string) throws UnsupportedEncodingException {
        return item.getString(string);
    }

    public String getString() {
        return item.getString();
    }

    public void write(File file) throws Exception {
        item.write(file);
    }

    public void delete() {
        item.delete();
    }

    public String getFieldName() {
        return item.getFieldName();
    }

    public void setFieldName(String string) {
        item.setFieldName(string);
    }

    public boolean isFormField() {
        return item.isFormField();
    }

    public void setFormField(boolean b) {
        item.setFormField(b);
    }
    
}
