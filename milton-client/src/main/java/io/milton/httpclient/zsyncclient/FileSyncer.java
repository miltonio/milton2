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
