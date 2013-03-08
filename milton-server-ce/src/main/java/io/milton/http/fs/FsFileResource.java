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

package io.milton.http.fs;

import io.milton.common.ContentTypeUtils;
import io.milton.common.RangeUtils;
import io.milton.common.ReadingException;
import io.milton.common.WritingException;
import io.milton.http.Auth;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.resource.*;
import java.io.*;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class FsFileResource extends FsResource implements CopyableResource, DeletableResource, GetableResource, MoveableResource, PropFindableResource, ReplaceableResource {

    private static final Logger log = LoggerFactory.getLogger(FsFileResource.class);
    
    private final FileContentService contentService;

    /**
     *
     * @param host - the requested host. E.g. www.mycompany.com
     * @param factory
     * @param file
     */
    public FsFileResource(String host, FileSystemResourceFactory factory, File file, FileContentService contentService) {
        super(host, factory, file);
        this.contentService = contentService;
    }

    @Override
    public Long getContentLength() {
        return file.length();
    }

    @Override
    public String getContentType(String preferredList) {
        String mime = ContentTypeUtils.findContentTypes(this.file);
        String s = ContentTypeUtils.findAcceptableContentType(mime, preferredList);
        if (log.isTraceEnabled()) {
            log.trace("getContentType: preferred: {} mime: {} selected: {}", new Object[]{preferredList, mime, s});
        }
        return s;
    }

    @Override
    public String checkRedirect(Request arg0) {
        return null;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotFoundException {
        InputStream in = null;
        try {
            in = contentService.getFileContent(file);
            if (range != null) {
                log.debug("sendContent: ranged content: " + file.getAbsolutePath());
                RangeUtils.writeRange(in, range, out);
            } else {
                log.debug("sendContent: send whole file " + file.getAbsolutePath());
                IOUtils.copy(in, out);
            }
            out.flush();
        } catch (FileNotFoundException e) {
            throw new NotFoundException("Couldnt locate content");
        } catch (ReadingException e) {
            throw new IOException(e);
        } catch (WritingException e) {
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * @{@inheritDoc}
     */
    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return factory.maxAgeSeconds(this);
    }

    /**
     * @{@inheritDoc}
     */
    @Override
    protected void doCopy(File dest) {
        try {
            FileUtils.copyFile(file, dest);
        } catch (IOException ex) {
            throw new RuntimeException("Failed doing copy to: " + dest.getAbsolutePath(), ex);
        }
    }

	@Override
	public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
		try {
			contentService.setFileContent(file, in);
		} catch (IOException ex) {
			throw new BadRequestException("Couldnt write to: " + file.getAbsolutePath(), ex);
		}
	}
}
