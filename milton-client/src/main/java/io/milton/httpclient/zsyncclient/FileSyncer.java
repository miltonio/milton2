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

package io.milton.httpclient.zsyncclient;

import io.milton.common.Path;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.httpclient.Host;
import io.milton.httpclient.HttpException;
import io.milton.httpclient.ProgressListener;
import io.milton.httpclient.Utils;
import java.io.File;
import java.io.IOException;

/**
 * Interface for various methods for syncronising local and remote files. Implementations will
 * efficiently update either the remote file (upload) or local file (downloade) transferring only
 * those bytes required to make the other file identical.
 *
 * @author brad
 */
public interface FileSyncer {
    File download(Host host, Path remotePath, File localFile, final ProgressListener listener) throws IOException, NotFoundException, HttpException, Utils.CancelledException, NotAuthorizedException, BadRequestException, ConflictException;
    
    void upload(Host host, File localcopy, Path remotePath, final ProgressListener listener) throws IOException, NotFoundException, Utils.CancelledException, NotAuthorizedException, ConflictException;
}
