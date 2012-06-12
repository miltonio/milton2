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
