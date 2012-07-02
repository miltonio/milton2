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

package com.mycompany;

import io.milton.common.StreamUtils;
import io.milton.common.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.ByteArrayOutputStream;

import io.milton.http.FileItem;
import io.milton.http.LockInfo;
import io.milton.http.LockResult;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.http.Request;
import io.milton.resource.*;

public class TFolderResource extends TTextResource implements PutableResource, MakeCollectionableResource, LockingCollectionResource {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TResource.class);
    
    ArrayList<TResource> children = new ArrayList<TResource>();
    
    public TFolderResource(TFolderResource parent, String name) {
        super(parent,name,"");
        log.debug( "created new folder: " + name);
    }
    
    @Override
    public Long getContentLength() {
        return null;
    }

	@Override
	public String getContentType(String accept) {
		return null;
	}
    
	
	
    @Override
    public String checkRedirect(Request request) {
        return null;
    }
    
	@Override
    public List<? extends Resource> getChildren() {
        return children;
    }
    
    @Override
    protected void sendContentMiddle(final PrintWriter printer) {
        super.sendContentMiddle(printer);
        printer.print("file upload field");
        printer.print("<form method='POST' enctype='multipart/form-data' action='" + this.getHref() + "'>");
        printer.print("<input type='file' name='file1' /><input type='submit'>");
        printer.print("</form>");
    }
    
    @Override
    protected void sendContentMenu(final PrintWriter printer) {
        printer.print("<ul>");
        for( TResource r : children ) {
            String href = Utils.escapeXml(r.getHref());
            String n = Utils.escapeXml(r.getName());
            print(printer, "<li><a href='" + href + "'>" + n + "</a>");
        }
        printer.print("</ul>");
    }
    
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        TFolderResource r = new TFolderResource(parent,name);
        for( TResource child : children ) {
            child.clone(r); // cstr adds to children
        }
        return r;
    }
    
    static ByteArrayOutputStream readStream(final InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamUtils.readTo(in, bos);
        return bos;
    }
    
	@Override
    public CollectionResource createCollection(String newName) {
        log.debug( "createCollection: " + newName);
        TFolderResource r = new TFolderResource(this,newName);
        return r;
    }
    
    @Override
    public String processForm(Map<String, String> params, Map<String, io.milton.http.FileItem> files) {
        super.processForm(params,files);
        log.debug( "folder processform");
        for( FileItem fitem : files.values()) {
            log.debug("found file: " + fitem.getName());
            ByteArrayOutputStream bos;
            try {
                bos = readStream(fitem.getInputStream());
            } catch (IOException ex) {
                log.error("error reading stream: ",ex );
                return null;
            }
            new TBinaryResource(this,fitem.getName(),bos.toByteArray(),null); // todo: infer content type from extension
        }
        return null;
    }

	@Override
    public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException {
        ByteArrayOutputStream bos = readStream(inputStream);
        log.debug("createNew: " + bos.size() + " - name: " + newName + " current child count: " + this.children.size());

        TResource r = new TBinaryResource(this,newName, bos.toByteArray(), contentType);
        log.debug("new child count: " + this.children.size());
        return r;
    }

	@Override
    public Resource child(String childName) {
        for( Resource r : getChildren() ) {
            if( r.getName().equals(childName)) return r;
        }
        return null;
    }

	@Override
    public LockToken createAndLock(String name, LockTimeout timeout, LockInfo lockInfo) {
        TTempResource temp = new TTempResource(this, name);
        LockResult r = temp.lock(timeout, lockInfo);
        if( r.isSuccessful() ) {
            return r.getLockToken();
        } else {
            throw new RuntimeException("didnt lock");
        }
    }
    
}
