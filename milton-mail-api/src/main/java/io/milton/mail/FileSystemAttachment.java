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
package io.milton.mail;

import io.milton.common.StreamUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import javax.mail.BodyPart;
import javax.mail.MessagingException;

/**
 * In memory representation of a file persisted to disk.
 *
 * Note that metadata is not persisted to disk, so this class is NOT suitable
 * for use in production environments, unless supplemented.
 */
public class FileSystemAttachment implements Attachment, Serializable {

    private static final long serialVersionUID = 1L;
    String name;
    String contentType;
    String disposition;
    String contentId;
    File file;

    public static FileSystemAttachment parse(BodyPart bp) {
        InputStream in = null;
        try {
            String name = bp.getFileName();
            if (name == null) {
                name = System.currentTimeMillis() + "";
            }
            String ct = bp.getContentType();
            String[] contentIdArr = bp.getHeader("Content-ID");
            String contentId = null;
            if (contentIdArr != null && contentIdArr.length > 0) {
                contentId = contentIdArr[0];
            }
            in = bp.getInputStream();
            File outFile = File.createTempFile(name, "attachment");
            FileOutputStream fout = new FileOutputStream(outFile);
            BufferedOutputStream bout = new BufferedOutputStream(fout);
            StreamUtils.readTo(in, bout);
            bout.flush();
            fout.flush();
            Utils.close(bout);
            Utils.close(fout);
            return new FileSystemAttachment(name, ct, outFile, contentId);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        } finally {
            Utils.close(in);
        }
    }

    public FileSystemAttachment(String name, String contentType, InputStream in, String contentId) {
        FileOutputStream fout = null;
        try {
            this.name = name;
            this.contentType = contentType;
            this.file = File.createTempFile(name, "attachment");
            this.contentId = contentId;            
            fout = new FileOutputStream(file);
            BufferedOutputStream bout = new BufferedOutputStream(fout);
            StreamUtils.readTo(in, bout);
            bout.flush();
            fout.flush();
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            Utils.close(fout);
        }

    }

    public FileSystemAttachment(String name, String contentType, File file, String contentId) {
        this.name = name;
        this.contentType = contentType;
        this.file = file;
        this.contentId = contentId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContentId() {
        return contentId;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getDisposition() {
        return disposition;
    }

    @Override
    public InputStream getInputStream() {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            return fin;
        } catch (IOException e) {
            throw new RuntimeException(file.getAbsolutePath(), e);
        }
    }

    @Override
    public void useData(InputStreamConsumer exec) {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            exec.execute(fin);
        } catch (IOException e) {
            throw new RuntimeException(file.getAbsolutePath(), e);
        } finally {
            Utils.close(fin);
        }
    }

    @Override
    public int size() {
        long l = file.length();
        return (int) l;
    }
}
