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

package com.ettrema.http.caldav.demo;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.io.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.ByteArrayOutputStream;

public class TFolderResource extends TResource implements PutableResource, MakeCollectionableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TResource.class );
    ArrayList<Resource> children = new ArrayList<Resource>();

    public TFolderResource( TFolderResource parent, String name ) {
        super( parent, name );
        log.debug( "created new folder: " + name );
    }

    @Override
    protected Object clone( TFolderResource newParent ) {
        TFolderResource newFolder = new TFolderResource( newParent, name );
        for( Resource child : parent.getChildren() ) {
            TResource res = (TResource) child;
            res.clone( newFolder ); // will auto-add to folder
        }
        return newFolder;
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    public String getContentType() {
        return null;
    }

    @Override
    public String checkRedirect( Request request ) {
        return null;
    }

    public List<? extends Resource> getChildren() {
        return children;
    }


    static ByteArrayOutputStream readStream( final InputStream in ) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamUtils.readTo( in, bos );
        return bos;
    }

    public CollectionResource createCollection( String newName ) {
        log.debug( "createCollection: " + newName );
        TFolderResource r = new TFolderResource( this, newName );
        return r;
    }

    public Resource createNew( String newName, InputStream inputStream, Long length, String contentType ) throws IOException {
        ByteArrayOutputStream bos = readStream( inputStream );
        log.debug( "createNew: " + bos.size() + " - name: " + newName + " current child count: " + this.children.size() );
        TResource r = new TBinaryResource( this, newName, bos.toByteArray(), contentType );
        log.debug( "new child count: " + this.children.size() );
        return r;
    }

    public Resource child( String childName ) {
        for( Resource r : getChildren() ) {
            if( r.getName().equals( childName ) ) return r;
        }
        return null;
    }

    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        PrintWriter pw = new PrintWriter(out);
        pw.print("<html><body>");
        pw.print("<h1>" + this.getName() + "</h1>");
        pw.print("<p>" + this.getClass().getCanonicalName() + "</p>");
        doBody(pw);
        pw.print("</body>");
        pw.print("</html>");
        pw.flush();
    }

    protected void doBody(PrintWriter pw) {
        pw.print("<ul>");
        for( Resource r : this.children ) {
            String href = r.getName();
            if( r instanceof CollectionResource) {
                href = href + "/";
            }
            pw.print("<li><a href='" + href + "'>" + r.getName() + "(" + r.getClass().getCanonicalName() + ")" + "</a></li>");
        }
        pw.print("</ul>");
    }

    public String getContentType( String accepts ) {
        return "text/html";
    }

    public String getCTag() {
        int x = this.hashCode();
        for(Resource r : this.children) {
            if( r instanceof TFolderResource) {
                TFolderResource tfr = (TFolderResource) r;
                x = x ^ tfr.getCTag().hashCode();
            } else {
                x = x ^ r.getUniqueId().hashCode();
            }
        }
        return "c" + x;
    }
}
