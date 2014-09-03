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

package io.milton.ftp;

import io.milton.common.Path;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiltonFsView implements FileSystemView {

    private static final Logger log = LoggerFactory.getLogger( MiltonFsView.class );
    Path homePath;
    CollectionResource home;
    Path currentPath;
    CollectionResource current;
    final ResourceFactory resourceFactory;
    final MiltonUser user;

    public MiltonFsView( Path homePath, CollectionResource current, ResourceFactory resourceFactory, MiltonUser user ) {
        super();
        this.user = user;
        if( homePath.isRelative() )
            throw new IllegalArgumentException( "homePath must be absolute" );
        this.homePath = homePath;
        this.currentPath = homePath;
        this.current = current;
        this.home = current;
        this.resourceFactory = resourceFactory;
        log.debug( "created view on resource: " + current.getName() + " for user: " + user.name + "@" + user.domain );
    }

    @Override
    public FtpFile getHomeDirectory() throws FtpException {
        return wrap( homePath, home );
    }

    @Override
    public FtpFile getWorkingDirectory() throws FtpException {
        return wrap( homePath, current );
    }

    @Override
    public boolean changeWorkingDirectory( String dir ) throws FtpException {
        try {
            log.debug( "cd: " + dir + " from " + currentPath );
            Path p = Path.path( dir );
            ResourceAndPath rp = getResource( p );
            if( rp.resource == null ) {
                log.debug( "not found: " + p );
                return false;
            } else if( rp.resource instanceof CollectionResource ) {
                current = (CollectionResource) rp.resource;
                currentPath = rp.path;
                log.debug( "currentPath is now: " + currentPath);
                return true;
            } else {
                log.debug( "not a collection: " + rp.resource.getName() );
                return false;
            }
        } catch (NotAuthorizedException ex) {
            throw new FtpException(ex);
        } catch (BadRequestException ex) {
            throw new FtpException(ex);
        }
    }

    @Override
    public FtpFile getFile( String path ) throws FtpException {
        try {
            log.debug( "getFile: " + path );
            if( path.startsWith( "." ) ) {
                path = currentPath.toString() + path.substring( 1 );
                log.debug( "getFile2: " + path );
            }
            Path p = Path.path( path );
            ResourceAndPath rp = getResource( p );
            if( rp.resource == null ) {
                log.debug( "returning new file" );
                return new MiltonFtpFile( this, rp.path, this.current, null, user );
            } else {
                return new MiltonFtpFile( this, rp.path, rp.resource, user );
            }
        } catch (NotAuthorizedException ex) {
            throw new FtpException(ex);
        } catch (BadRequestException ex) {
            throw new FtpException(ex);
        }
    }

    @Override
    public boolean isRandomAccessible() throws FtpException {
        return true;
    }

    @Override
    public void dispose() {
    }

    public ResourceAndPath getResource( Path p ) throws NotAuthorizedException, BadRequestException {
        log.debug( "getResource: " + p );
        if( p.isRelative() ) {
            p = Path.path( currentPath.toString() + '/' + p.toString() );
            Resource r = resourceFactory.getResource( user.domain, p.toString() );
            return new ResourceAndPath( r, p );
        } else {
            Resource r = resourceFactory.getResource( user.domain, p.toString() );
            return new ResourceAndPath( r, p );
        }
    }

    public FtpFile wrap( Path path, Resource r ) {
        return new MiltonFtpFile( this, path, r, user );
    }

    /**
     * Represents a resource (possibly null) and an absolute path (never null)
     */
    public static class ResourceAndPath {

        final Resource resource;
        final Path path;

        public ResourceAndPath( Resource r, Path p ) {
            if( p == null )
                throw new IllegalArgumentException( "path may not be null" );
            if( p.isRelative() )
                throw new IllegalArgumentException( "path must be absolute" );
            this.resource = r;
            this.path = p;
        }
    }
}
