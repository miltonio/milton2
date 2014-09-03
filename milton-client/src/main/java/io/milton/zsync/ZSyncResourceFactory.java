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

package io.milton.zsync;

import io.milton.common.BufferingOutputStream;
import io.milton.common.Path;
import io.milton.http.Request.Method;
import io.milton.http.*;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.common.LogUtils;
import io.milton.common.StreamUtils;
import io.milton.resource.*;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This resource factory allows resouces to be retrieved and updated using the
 * zsync protocol.
 *
 * Client side process for updating a local file from a server file a) assume
 * the remote file is at path /somefile b) retrieve zsync metadata (ie headers
 * and checksums) GET /somefile/.zsync c) implement rolling checksums and
 * retrieve ranges of real file as needed with partial GETs GET /somefile
 * Ranges: x-y, n-m, etc d) merge the partial ranges
 *
 *
 * Client side process for updating a server file with a local file a) assume
 * the remote file is at path /somefile b) retrieve zsync metadata (ie headers
 * and checksums) GET /somefile/.zsync c) Calculate instructions and range data
 * to send to server, based on the retrieved checksums d) send to server
 *
 *
 * ....
 */
public class ZSyncResourceFactory implements ResourceFactory {

    private static final Logger log = LoggerFactory.getLogger(ZSyncResourceFactory.class);
    private String suffix = ".zsync";
    private final ResourceFactory wrapped;
    private MetaFileMaker metaFileMaker;
    private int defaultBlockSize = 512;
    private int maxMemorySize = 100000;

    public ZSyncResourceFactory(ResourceFactory wrapped) {
        this.wrapped = wrapped;
        metaFileMaker = new MetaFileMaker();
    }

    @Override
    public Resource getResource(String host, String path) throws NotAuthorizedException, BadRequestException {
        if (path.endsWith("/" + suffix)) {
            Path p = Path.path(path);
            String realPath = p.getParent().toString();
            Resource r = wrapped.getResource(host, realPath);
            if (r == null) {
                return new ZSyncAdapterResource(null, realPath, host); // will throw bad request
            } else {
                if (r instanceof GetableResource) {
                    LogUtils.trace(log, "Found existing compatible resource at", realPath);
                    return new ZSyncAdapterResource((GetableResource) r, realPath, host);
                } else {
                    return new ZSyncAdapterResource(null, realPath, host); // will throw bad request
                }
            }
        } else {
            return wrapped.getResource(host, path);
        }
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public ResourceFactory getWrapped() {
        return wrapped;
    }

    public class ZSyncAdapterResource implements GetableResource, ReplaceableResource, DigestResource {

        private final GetableResource r;
        private final String realPath;
        private final String host;
        /**
         * populated on POST, then used in sendContent
         */
        private List<Range> ranges;

        public ZSyncAdapterResource(GetableResource r, String realPath, String host) {
            this.r = r;
            this.realPath = realPath;
            this.host = host;
        }

        @Override
        public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
            if (r == null) {
                throw new BadRequestException(this, "No existing resource was found to map the zsync operation to");
            }
            if (ranges != null) {
                log.info("sendContent: sending range data");
                sendRangeData(out);
            } else {
                log.info("sendContent: sending meta data");
                sendMetaData(params, contentType, out);
            }

        }

        @Override
        public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
            if (r == null) {
                throw new BadRequestException(this, "No existing resource was found to map the zsync operation to");
            }

            log.trace("ZSync Replace Content: uploaded bytes " + length);

            try {

                File prevFile = File.createTempFile("milton-zsync", "prevFile");
                FileOutputStream fout = new FileOutputStream(prevFile);
                r.sendContent(fout, null, null, null);
                StreamUtils.close(fout);
                log.trace("Saved previous file to " + prevFile.getAbsolutePath());

                File uploadData = File.createTempFile("milton-zsync", "uploadData");
                fout = new FileOutputStream(uploadData);
                StreamUtils.readTo(in, fout);
                StreamUtils.close(fout);
                log.trace("Saved PUT data to " + uploadData.getAbsolutePath());

                File newFile = null;
                InputStream fin = null;
                BufferedInputStream uploadIn = null;

                try {
                    fin = new FileInputStream(uploadData);
                    uploadIn = new BufferedInputStream(fin);
                    UploadReader um = new UploadReader(prevFile, uploadIn);
                    newFile = um.assemble();
                    log.trace("Assembled file and saved to " + newFile.getAbsolutePath());

                    String actChecksum = new SHA1(newFile).SHA1sum();
                    String expChecksum = um.getChecksum();

                    if (!actChecksum.equals(expChecksum)) {
                        throw new RuntimeException("Computed SHA1 checksum doesn't match expected checksum\n" + "\tExpected: " + expChecksum + "\n" + "\tActual: " + actChecksum + "\n in temp file: " + newFile.getAbsolutePath());
                    }


                } finally {
                    StreamUtils.close(uploadIn);
                    StreamUtils.close(fin);
                }

                updateResourceContentActual(newFile);

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        private void sendMetaData(Map<String, String> params, String contentType, OutputStream out) throws RuntimeException {
            Long fileLength = r.getContentLength();
            int blocksize = defaultBlockSize;
            if (fileLength != null) {
                blocksize = metaFileMaker.computeBlockSize(fileLength);
            }

            MetaFileMaker.MetaData metaData;
            if (r instanceof ZSyncResource) {
                ZSyncResource zr = (ZSyncResource) r;
                metaData = zr.getZSyncMetaData();
            } else {
                BufferingOutputStream bufOut = new BufferingOutputStream(maxMemorySize);
                try {
                    r.sendContent(bufOut, null, params, contentType);
                    bufOut.flush();
                } catch (Exception ex) {
                    bufOut.deleteTempFileIfExists();
                    throw new RuntimeException(ex);
                } finally {
                    StreamUtils.close(bufOut);
                }
                InputStream in = bufOut.getInputStream();
                try {
                    metaData = metaFileMaker.make(realPath, blocksize, fileLength == null ? 0 : fileLength.longValue(), r.getModifiedDate(), in);
                } finally {
                    StreamUtils.close(in);
                }
            }
            metaFileMaker.write(metaData, out);
        }

        private void updateResourceContentActual(File mergedFile) throws FileNotFoundException, BadRequestException, ConflictException, NotAuthorizedException, IOException {
            if (r instanceof ReplaceableResource) {
                log.trace("updateResourceContentActual: " + mergedFile.getAbsolutePath() + ", resource is replaceable");
                FileInputStream fin = null;
                try {
                    fin = new FileInputStream(mergedFile);
                    ReplaceableResource rr = (ReplaceableResource) r;
                    rr.replaceContent(fin, mergedFile.length());
                } finally {
                    StreamUtils.close(fin);
                }
            } else {
                log.trace("updateResourceContentActual: " + mergedFile.getAbsolutePath() + ", resource is NOT replaceable, try to replace through parent");
                String parentPath = Path.path(realPath).getParent().toString();
                Resource rParent = wrapped.getResource(host, parentPath);
                if (rParent == null) {
                    throw new RuntimeException("Failed to locate parent resource to update contents. parent: " + parentPath + " host: " + host);
                }
                if (rParent instanceof PutableResource) {
                    log.trace("found parent resource, implements PutableResource");
                    FileInputStream fin = null;
                    try {
                        fin = new FileInputStream(mergedFile);
                        PutableResource putable = (PutableResource) rParent;
                        putable.createNew(r.getName(), fin, mergedFile.length(), r.getContentType(null));
                    } finally {
                        StreamUtils.close(fin);
                    }
                } else {
                    throw new RuntimeException("Tried to update non-replaceable resource by doing createNew on parent, but the parent doesnt implement PutableResource. parent path: " + parentPath + " host: " + host + " parent type: " + rParent.getClass());
                }
            }


        }

        @Override
        public Long getMaxAgeSeconds(Auth auth) {
            return null;
        }

        @Override
        public String getContentType(String accepts) {
            return "application/zsyncM";
        }

        @Override
        public Long getContentLength() {
            return null;
        }

        @Override
        public String getUniqueId() {
            return null;
        }

        @Override
        public String getName() {
            return suffix;
        }

        @Override
        public Object authenticate(String user, String password) {
            if (r == null) {
                return "ok"; // will fail with 400 anyway
            }
            return r.authenticate(user, password);
        }

        @Override
        public boolean authorise(Request request, Method method, Auth auth) {
            if (r == null) {
                return true; // will fail anyway
            }
            return r.authorise(request, method, auth);
        }

        @Override
        public String getRealm() {
            if (r == null) {
                return "Realm";
            }
            return r.getRealm();
        }

        @Override
        public Date getModifiedDate() {
            if (r == null) {
                return null;
            }
            return r.getModifiedDate();
        }

        @Override
        public String checkRedirect(Request request) {
            return null;
        }

        @Override
        public Object authenticate(DigestResponse digestRequest) {
            return ((DigestResource) r).authenticate(digestRequest);
        }

        @Override
        public boolean isDigestAllowed() {
            return (r instanceof DigestResource) && ((DigestResource) r).isDigestAllowed();
        }

        private void sendRangeData(OutputStream out) {
            PrintWriter pw = new PrintWriter(out);
            for (Range range : ranges) {
                pw.println(range.getRange());
            }
            pw.flush();
        }
    }
}
