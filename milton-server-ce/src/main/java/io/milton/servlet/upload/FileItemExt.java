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
import org.apache.commons.fileupload.FileItemHeaders;

public class FileItemExt implements DataSource, FileItem {
    
    private static final long serialVersionUID = 1L;
    
    private final FileItem item;
	private FileItemHeaders headers;
    
    public FileItemExt(FileItem item) {
        this.item = item;
    }

	@Override
    public InputStream getInputStream() throws IOException {
        return item.getInputStream();
    }

	@Override
    public OutputStream getOutputStream() throws IOException {
        return item.getOutputStream();
    }

	@Override
    public String getContentType() {
        return item.getContentType();
    }

	@Override
    public String getName() {
        return item.getName();
    }

	@Override
    public boolean isInMemory() {
        return item.isInMemory();
    }

	@Override
    public long getSize() {
        return item.getSize();
    }

	@Override
    public byte[] get() {
        return item.get();
    }

	@Override
    public String getString(String string) throws UnsupportedEncodingException {
        return item.getString(string);
    }

	@Override
    public String getString() {
        return item.getString();
    }

	@Override
    public void write(File file) throws Exception {
        item.write(file);
    }

	@Override
    public void delete() {
        item.delete();
    }

	@Override
    public String getFieldName() {
        return item.getFieldName();
    }

	@Override
    public void setFieldName(String string) {
        item.setFieldName(string);
    }

	@Override
    public boolean isFormField() {
        return item.isFormField();
    }

	@Override
    public void setFormField(boolean b) {
        item.setFormField(b);
    }

	@Override
	public FileItemHeaders getHeaders() {
		return headers;
	}

	@Override
	public void setHeaders(FileItemHeaders fih) {
		this.headers = fih;
	}
    
}
