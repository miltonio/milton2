/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * This MOCK class just pretends to be a database. In reality you would use a DAO
 * or something to access a real database
 *
 * @author brad
 */
public class MyDatabase {
    
    private static FolderContentItem root;

    public static FolderContentItem getRoot() {
        if( root == null ) {
            root = new FolderContentItem(null, null);
            root.addFolder("folder1");
            root.addFolder("folder2");
            root.addFolder("folder3");
        }
        return root;
    }
        
    
    public abstract static class AbstractContentItem {
        private UUID id; // this for a uniqueId
        private String name;
        private FolderContentItem parent;
        private Date createdDate;
        protected Date modifiedDate;

        public abstract AbstractContentItem copyTo(FolderContentItem newParent, String newName);
        
        public AbstractContentItem(String name, FolderContentItem parent) {
            this.name = name;
            this.parent = parent;
            this.id = UUID.randomUUID();
            this.createdDate = new Date();
            this.modifiedDate = new Date();
        }

        public UUID getId() {
            return id;
        }

        public Date getCreatedDate() {
            return createdDate;
        }

        public Date getModifiedDate() {
            return modifiedDate;
        }
                                
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.modifiedDate = new Date();
            this.name = name;
        }                

        public FolderContentItem getParent() {
            return parent;
        }     
        
        public void moveTo(FolderContentItem newParent) {
            this.modifiedDate = new Date();
            parent.children.remove(this);
            newParent.children.add(this);
            this.parent = newParent;
        }
        
        public void delete() {
            parent.children.remove(this);
        }        
    }
    
    public static class FolderContentItem extends AbstractContentItem {
        private List<AbstractContentItem> children = new ArrayList<AbstractContentItem>();

        public FolderContentItem(String name, FolderContentItem parent) {
            super(name, parent);
        }        

        @Override
        public FolderContentItem copyTo(FolderContentItem newParent, String newName) {
            FolderContentItem newFolder = newParent.addFolder(newName);
            for( AbstractContentItem r : children) {
                r.copyTo(newFolder, r.getName());
            }
            return newFolder;
        }
        
        
        
        public FileContentItem addFile(String name) {
            FileContentItem f = new FileContentItem(name, this);
            children.add(f);
            return f;
        }
        
        public FolderContentItem addFolder(String name) {
            FolderContentItem f = new FolderContentItem(name, this);
            children.add(f);
            return f;
        }

        public void remove(String name ) {
            Iterator<AbstractContentItem> it = children.iterator();
            while( it.hasNext() ) {
                if( it.next().getName().equals(name)) {
                    it.remove();
                }
            }
        }
        
        public List<AbstractContentItem> getChildren() {
            return children;
        }
        
        public AbstractContentItem child(String name) {
            for( AbstractContentItem r : children) {
                if( r.getName().equals(name)) {
                    return r;
                }
            }
            return null;
        }               
    }
    
    public static class FileContentItem extends AbstractContentItem {
        private byte[] bytes;
        private String contentType;

        public FileContentItem(String name, FolderContentItem parent) {
            super(name, parent);
        }

        @Override
        public FileContentItem copyTo(FolderContentItem newParent, String newName) {
            FileContentItem f = newParent.addFile(newName);
            f.bytes = bytes;
            f.contentType = contentType;
            return f;
        }
        
        
        
        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.modifiedDate = new Date();
            this.bytes = bytes;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }                        
    }
}
