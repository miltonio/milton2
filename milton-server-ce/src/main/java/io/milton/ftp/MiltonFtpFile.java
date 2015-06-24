/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.milton.ftp;

import io.milton.common.BufferingOutputStream;
import io.milton.common.Path;
import io.milton.http.Auth;
import io.milton.resource.ReplaceableResource;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.resource.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.ftpserver.ftplet.FtpFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter to between apache FTP's FtpFile interface and milton's Resource interface
 *
 * This class can wrap any Resource instance and will allow or disallow requests
 * as appropriate given what methods the resource instance supports - E.g. DeletableResource,
 * MoveableResource, etc
 *
 * @author u370681
 */
public class MiltonFtpFile implements FtpFile {

    private static final Logger log = LoggerFactory.getLogger( MiltonFtpFile.class );
    private final Path path;
    private CollectionResource parent;
    private final MiltonFsView ftpFactory;
    private Resource r;
    private final MiltonUser user;

    public MiltonFtpFile( MiltonFsView resourceFactory, Path path, Resource r, MiltonUser user ) {
        this.path = path;
        this.r = r;
        this.parent = null;
        this.ftpFactory = resourceFactory;
        this.user = user;
    }

    public MiltonFtpFile( MiltonFsView resourceFactory, Path path, CollectionResource parent, Resource r, MiltonUser user ) {
        this.path = path;
        this.r = null;
        this.parent = parent;
        this.ftpFactory = resourceFactory;
        this.user = user;
    }

    public String getAbsolutePath() {
        return path.toString();
    }

    public String getName() {
        return r.getName();
    }

    public boolean isHidden() {
        return false;
    }

    public boolean isDirectory() {
        return ( r instanceof CollectionResource );
    }

    public boolean isFile() {
        return !isDirectory();
    }

    public boolean doesExist() {
        return r != null;
    }

    @Override
    public boolean isReadable() {
        log.debug( "isReadble" );
        if( r == null || !( r instanceof GetableResource ) ) return false;

        Auth auth = new Auth( user.getName(), user.getUser() );
        FtpRequest request = new FtpRequest( Method.GET, auth, path.toString() );
        return r.authorise( request, request.getMethod(), auth );

    }

    /**
     * Check file write permission.
     */
    @Override
    public boolean isWritable() {
        try {
            log.debug( "isWritable: " + getAbsolutePath() );
            if( path.isRoot() ) return false;
            Auth auth = new Auth( user.getName(), user.getUser() );
            FtpRequest request = new FtpRequest( Method.DELETE, auth, path.toString() );
            if( r != null ) {
                if( r instanceof ReplaceableResource ) {
                    return r.authorise( request, Method.PUT, auth );
                }
            }
            if( getParent() instanceof PutableResource ) {
                return getParent().authorise( request, Method.PUT, auth );
            } else {
                return false;
            }
        } catch (NotAuthorizedException ex) {
            throw new RuntimeException(ex);
        } catch (BadRequestException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean isRemovable() {
        log.debug( "isRemovable: " + getAbsolutePath() );
        if( r == null ) return false;
        if( path.isRoot() ) return false;
        Auth auth = new Auth( user.getName(), user.getUser() );
        FtpRequest request = new FtpRequest( Method.DELETE, auth, path.toString() );
        boolean b = r.authorise( request, Method.DELETE, auth );
        log.debug( ".. = " + b );
        return b;
    }

    public String getOwnerName() {
        return "anyone";
    }

    public String getGroupName() {
        return "anygroup";
    }

    public int getLinkCount() {
        return 0;
    }

    public long getLastModified() {
        if( r.getModifiedDate() != null ) {
            return r.getModifiedDate().getTime();
        } else {
            return System.currentTimeMillis();
        }
    }

    public boolean setLastModified( long time ) {
        return false;
    }

    public long getSize() {
        if( r instanceof GetableResource ) {
            GetableResource gr = (GetableResource) r;
            Long ll = gr.getContentLength();
            if( ll == null ) return 0;
            return ll.longValue();
        } else {
            return 0;
        }
    }

    public boolean mkdir() {
        log.debug( "mkdir: " + this.path );
        if( parent != null ) {
            if( parent instanceof MakeCollectionableResource ) {
                MakeCollectionableResource mcr = (MakeCollectionableResource) parent;
                try {
                    r = mcr.createCollection( path.getName() );
                    return true;
                } catch( NotAuthorizedException ex ) {
                    log.debug( "no authorised" );
                    return false;
                } catch( BadRequestException ex ) {
                    return false;
                } catch( ConflictException ex ) {
                    log.debug( "conflict" );
                    return false;
                }
            } else {
                log.debug( "parent does not support creating collection" );
                return false;
            }
        } else {
            throw new RuntimeException( "no parent" );
        }
    }

    @Override
    public boolean delete() {
        if( r instanceof DeletableResource ) {
            DeletableResource dr = (DeletableResource) r;
            try {
                dr.delete();
            } catch( NotAuthorizedException ex ) {
                log.warn( "can't delete, not authorised" );
                return false;
            } catch( ConflictException ex ) {
                log.warn( "can't delete, conflct" );
                return false;
            } catch( BadRequestException ex ) {
                log.warn( "can't delete, bad request" );
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean move( FtpFile newFile ) {
        if( r == null ) {
            throw new RuntimeException( "resource not saved yet" );
        } else if( r instanceof MoveableResource ) {
            try {
                MoveableResource src = (MoveableResource) r;
                MiltonFtpFile dest = (MiltonFtpFile) newFile;
                CollectionResource crDest;
                crDest = dest.getParent();
                String newName = dest.path.getName();
                try {
                    src.moveTo( crDest, newName );
                    return true;
                } catch( BadRequestException ex ) {
                    log.error( "bad request, can't move", ex );
                    return false;
                } catch( NotAuthorizedException ex ) {
                    log.error( "not authorised can't move", ex );
                    return false;
                } catch( ConflictException ex ) {
                    log.error( "can't move", ex );
                    return false;
                }
            } catch( NotAuthorizedException ex ) {
                throw new RuntimeException(ex);
            } catch (BadRequestException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            log.debug( "not moveable: " + this.getName() );
            return false;
        }
    }

    @Override
    public List<FtpFile> listFiles() {
        log.debug( "listfiles" );
        List<FtpFile> list = new ArrayList<FtpFile>();
        if( r instanceof CollectionResource ) {
            try {
                CollectionResource cr = (CollectionResource) r;
                for( Resource child : cr.getChildren() ) {
                    list.add( ftpFactory.wrap( path.child( child.getName() ), child ) );
                }
            } catch (NotAuthorizedException ex) {
                throw new RuntimeException(ex);
            } catch (BadRequestException ex) {
                throw new RuntimeException(ex);
            }
        }
        return list;
    }

    @Override
    public OutputStream createOutputStream( long offset ) throws IOException {
        log.debug( "createOutputStream: " + offset );
        final BufferingOutputStream out = new BufferingOutputStream( 50000 );
        if( r instanceof ReplaceableResource ) {
            log.debug( "resource is replaceable" );
            final ReplaceableResource rr = (ReplaceableResource) r;
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    try {
                        rr.replaceContent(out.getInputStream(), out.getSize());
                    } catch (BadRequestException ex) {
                        throw new RuntimeException(ex);
                    } catch (ConflictException ex) {
                        throw new RuntimeException(ex);
                    } catch (NotAuthorizedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
            out.setOnClose( runnable );
            return out;
        } else {
            CollectionResource col;
            try {
                col = getParent();
            } catch (NotAuthorizedException ex) {
                throw new RuntimeException(ex);
            } catch (BadRequestException ex) {
                throw new RuntimeException(ex);
            }
            if( col == null ) {
                throw new IOException( "parent not found" );
            } else if( col instanceof PutableResource ) {
                final PutableResource putableResource = (PutableResource) col;
                final String newName = path.getName();
                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        try {
                            putableResource.createNew( newName, out.getInputStream(), out.getSize(), null );
                        } catch( BadRequestException ex ) {
                            throw new RuntimeException( ex );
                        } catch( NotAuthorizedException ex ) {
                            throw new RuntimeException( ex );
                        } catch( ConflictException ex ) {
                            throw new RuntimeException( ex );
                        } catch( IOException ex ) {
                            throw new RuntimeException( ex );
                        }
                    }
                };
                out.setOnClose( runnable );
                return out;
            } else {
                throw new IOException( "folder doesnt support PUT, and the resource is not replaceable" );
            }
        }
    }

	@Override
    public InputStream createInputStream( long offset ) throws IOException {
        if( r instanceof GetableResource ) {
            GetableResource gr = (GetableResource) r;
            String ct = gr.getContentType( null );
            BufferingOutputStream out = new BufferingOutputStream( 50000 );
            try {
                gr.sendContent( out, null, null, ct );
                out.close();
                return out.getInputStream();
            } catch (NotFoundException ex) {
				log.warn("Not found exception", ex);
				return null;
			} catch( BadRequestException ex ) {
                log.warn( "bad request", ex );
                return null;
            } catch( NotAuthorizedException ex ) {
                log.warn( "not authorising", ex );
                return null;
            }
        } else {
            return null;
        }
    }

    private CollectionResource getParent() throws NotAuthorizedException, BadRequestException {
        if( parent == null ) {
            MiltonFsView.ResourceAndPath rp = ftpFactory.getResource( path.getParent() );
            if( rp.resource == null ) {
                throw new RuntimeException( "couldnt find parent: " + path );
            } else {
                parent = (CollectionResource) rp.resource;
            }
        }
        return parent;
    }
}
