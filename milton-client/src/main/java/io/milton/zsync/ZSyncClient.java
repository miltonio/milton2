/*
 * Copyright (C) 2012 McEvoy Software Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package io.milton.zsync;

import io.milton.common.Path;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.common.LogUtils;
import io.milton.httpclient.*;
import io.milton.httpclient.Utils.CancelledException;
import io.milton.httpclient.zsyncclient.FileSyncer;
import io.milton.httpclient.zsyncclient.HttpRangeLoader;
import java.io.File;
import java.io.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bradm
 */
public class ZSyncClient implements FileSyncer{

    private static final Logger log = LoggerFactory.getLogger(ZSyncClient.class);
    private TransferService transferService;
    private final FileMaker fileMaker;
    private int blocksize = 256;

    public ZSyncClient(TransferService transferService) {
        this.transferService = transferService;
        fileMaker = new FileMaker();
    }

    /**
     *
     * @param host
     * @param remotePath
     * @param downloadTo
     * @return - the assembled file, which probably needs to be moved to replace
     * the previous file
     * @throws IOException
     * @throws HttpException
     * @throws NotFoundException - if the remote file does not exist
     */
    @Override
    public File download(Host host, Path remotePath, File localFile, final ProgressListener listener) throws IOException, NotFoundException, HttpException, CancelledException, NotAuthorizedException, BadRequestException, ConflictException {
        LogUtils.trace(log, "download", host, remotePath);
        final File fRemoteMeta = File.createTempFile("zsync-meta", remotePath.getName());
        String url = host.getHref(remotePath.child(".zsync"));
        boolean notExisting = false;
        try {
            transferService.get(url, new StreamReceiver() {

                @Override
                public void receive(InputStream in) throws IOException {
                    if (listener != null && listener.isCancelled()) {
                        throw new CancelledException();
                    }
                    FileOutputStream fout = null;
                    try {
                        fout = new FileOutputStream(fRemoteMeta);
                        Utils.writeBuffered(in, fout, listener);
                    } catch (CancelledException cancelled) {
                        throw cancelled;
                    } catch (IOException ex) {
                        throw ex;
                    }
                }
            }, null, listener, null);
        } catch (BadRequestException e) {
            notExisting = true;
        }
        io.milton.httpclient.File remoteFile = (io.milton.httpclient.File) host.find(remotePath.toString());
        if (notExisting) {
            throw new NotFoundException(url);
        } else {
            // Now build local file			
            HttpRangeLoader rangeLoader = new HttpRangeLoader(remoteFile, listener);
            try {
                return fileMaker.make(localFile, fRemoteMeta, rangeLoader);
            } catch (Exception e) {
                if (e instanceof CancelledException) {
                    throw (CancelledException) e;
                } else if (e instanceof HttpException) {
                    throw (HttpException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }

        }
    }

    /**
     *
     * @param host
     * @param localcopy
     * @param remotePath
     * @return the number of bytes uploaded
     * @throws IOException
     * @throws HttpException
     */
    @Override
    public void upload(Host host, File localcopy, Path remotePath, final ProgressListener listener) throws IOException, NotFoundException, CancelledException, NotAuthorizedException, ConflictException {
        final File fRemoteMeta = File.createTempFile("zsync", remotePath.getName());
        String baseUrl = host.getHref(remotePath);
        String url = baseUrl + "/.zsync";
        try {
            transferService.get(url, new StreamReceiver() {

                @Override
                public void receive(InputStream in) throws IOException {
                    OutputStream fout = new FileOutputStream(fRemoteMeta);
                    Utils.writeBuffered(in, fout, listener);
                }
            }, null, listener, null);
        } catch (BadRequestException e) {
            throw new NotFoundException(url);
        } catch (HttpException e) {
                throw new RuntimeException(e);
        }


        UploadMaker umx = new UploadMaker(localcopy, fRemoteMeta);
        InputStream uploadIn = null;
        try {
            uploadIn = umx.makeUpload();
            transferService.put(url, uploadIn, null, null, null, listener, null);
        } finally {
            IOUtils.closeQuietly(uploadIn);
            FileUtils.deleteQuietly(fRemoteMeta);
        }
    }

    public int getBlocksize() {
        return blocksize;
    }

    public void setBlocksize(int blocksize) {
        this.blocksize = blocksize;
    }
}
