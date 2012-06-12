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

package com.mycompany;

import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PropPatchableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Utils;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.http11.auth.DigestGenerator;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.http.webdav.PropPatchHandler.Fields;
import java.text.DateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class TResource implements GetableResource, PropFindableResource, DeletableResource, MoveableResource,
    CopyableResource, PropPatchableResource
//	, LockableResource
    , DigestResource
{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TResource.class );
    String name;
    Date modDate;
    Date createdDate;
    TFolderResource parent;
    TLock lock;
    String user;
    String password;

    protected abstract Object clone( TFolderResource newParent );

    public TResource( TFolderResource parent, String name ) {
        this.parent = parent;
        this.name = name;
		try {
			//modDate = new Date();
			//createdDate = new Date();
			createdDate = DateFormat.getDateInstance(DateFormat.SHORT).parse("1/1/2000");
			modDate = DateFormat.getDateInstance(DateFormat.SHORT).parse("1/1/2005");
		} catch (ParseException ex) {
			Logger.getLogger(TResource.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		
        if( parent != null ) {
            this.user = parent.user;
            this.password = parent.password;
            checkAndRemove( parent, name );
            parent.children.add( this );
        }
    }

    public void setSecure( String user, String password ) {
        this.user = user;
        this.password = password;
    }

    public String getHref() {
        if( parent == null ) {
            return "/webdav/";
        } else {
            String s = parent.getHref();
            if( !s.endsWith( "/" ) ) s = s + "/";
            s = s + name;
            if( this instanceof CollectionResource ) s = s + "/";
            return s;
        }
    }

    public Long getContentLength() {
        return null;
    }

    public String checkRedirect( Request request ) {
        return null;
    }

    public Long getMaxAgeSeconds( Auth auth ) {
        return (long) 10;
    }

    public void moveTo( CollectionResource rDest, String name ) {
        log.debug( "moving.." );
        TFolderResource d = (TFolderResource) rDest;
        this.parent.children.remove( this );
        this.parent = d;
        this.parent.children.add( this );
        this.name = name;
    }

    public Date getCreateDate() {
        return createdDate;
    }

    public String getName() {
        return name;
    }

    public Object authenticate( String user, String requestedPassword ) {
        log.debug( "authentication: " + user + " - " + requestedPassword + " = " + password );
        return "ok";
    }

    public Object authenticate( DigestResponse digestRequest ) {
        if( this.user == null ) {
            log.debug( "no user defined, so allow access" );
            return "ok";
        }

        DigestGenerator dg = new DigestGenerator();
        String serverResponse = dg.generateDigest( digestRequest, password );
        String clientResponse = digestRequest.getResponseDigest();

        log.debug( "server resp: " + serverResponse );
        log.debug( "given response: " + clientResponse );

        if( serverResponse.equals( clientResponse ) ) {
            return "ok";  // return any non-null value to indicate success. Normally this will be a user object
        } else {
            return null;
        }
    }

    public boolean isDigestAllowed() {
        return true;
    }
    
    

    public boolean authorise( Request request, Method method, Auth auth ) {
        log.debug( "authorise" );
        if( auth == null ) {
            return false;

        } else {
            return true;
        }
    }

    public String getRealm() {
        return "testrealm@host.com";
    }

    public Date getModifiedDate() {
        return modDate;


    }

    public void delete() {
        if( this.parent == null )
            throw new RuntimeException( "attempt to delete root" );

        if( this.parent.children == null )
            throw new NullPointerException( "children is null" );
        this.parent.children.remove( this );
    }

    public void copyTo( CollectionResource toCollection, String name ) {
        TResource rClone;
        rClone = (TResource) this.clone( (TFolderResource) toCollection );
        rClone.name = name;
    }

    public int compareTo( Resource o ) {
        if( o instanceof TResource ) {
            TResource res = (TResource) o;
            return this.getName().compareTo( res.getName() );
        } else {
            return -1;
        }
    }

    public String getUniqueId() {
        return this.hashCode() + "";
    }

    public LockToken getCurrentLock() {
        if( this.lock == null ) return null;
        LockToken token = new LockToken();
        token.info = this.lock.lockInfo;
        token.timeout = new LockTimeout( this.lock.seconds );
        token.tokenId = this.lock.lockId;


        return token;


    }

    public LockResult lock( LockTimeout timeout, LockInfo lockInfo ) {
			System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX  locking");
//        if( lock != null ) {
//            // todo
//            throw new RuntimeException("already locked");
//        }

        LockTimeout.DateAndSeconds lockedUntil = timeout.getLockedUntil( 60l, 3600l );

        this.lock = new TLock( lockedUntil.date, UUID.randomUUID().toString(), lockedUntil.seconds, lockInfo );

        LockToken token = new LockToken();
        token.info = lockInfo;
        token.timeout = new LockTimeout( lockedUntil.seconds );
        token.tokenId = this.lock.lockId;

        return LockResult.success( token );
    }

    public LockResult refreshLock( String token ) {
        if( lock == null ) throw new RuntimeException( "not locked" );
        if( !lock.lockId.equals( token ) )
            throw new RuntimeException( "invalid lock id" );
        this.lock = lock.refresh();
        LockToken tok = makeToken();
        return LockResult.success( tok );
    }

    public void unlock( String tokenId ) {
        if( lock == null ) {
            log.warn( "request to unlock not locked resource" );
            return;
        }
        if( !lock.lockId.equals( tokenId ) )
            throw new RuntimeException( "Invalid lock token" );
        this.lock = null;
    }

    LockToken makeToken() {
        LockToken token = new LockToken();
        token.info = lock.lockInfo;
        token.timeout = new LockTimeout( lock.seconds );
        token.tokenId = lock.lockId;
        return token;
    }

    private void checkAndRemove( TFolderResource parent, String name ) {
        TResource r = (TResource) parent.child( name );
        if( r != null ) parent.children.remove( r );
    }

    /**
     * This is required for the PropPatchableResource interface, but should
     * not be implemented.
     *
     * Implement CustomPropertyResource or MultiNamespaceCustomPropertyResource instead
     *
     * @param fields
     */
    public void setProperties( Fields fields ) {
    }

    protected void print( PrintWriter printer, String s ) {
        printer.print( s );
    }

    class TLock {

        final Date lockedUntil;
        final String lockId;
        final long seconds;
        final LockInfo lockInfo;

        public TLock( Date lockedUntil, String lockId, long seconds, LockInfo lockInfo ) {
            this.lockedUntil = lockedUntil;
            this.lockId = lockId;
            this.seconds = seconds;
            this.lockInfo = lockInfo;
        }

        TLock refresh() {
            Date dt = Utils.addSeconds( Utils.now(), seconds );
            return new TLock( dt, lockId, seconds, lockInfo );
        }
    }
}
