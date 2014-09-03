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

import com.mycompany.MyDatabase.FileContentItem;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.Copy;
import io.milton.annotations.CreatedDate;
import io.milton.annotations.Delete;
import io.milton.annotations.DisplayName;
import io.milton.annotations.Get;
import io.milton.annotations.MakeCollection;
import io.milton.annotations.ModifiedDate;
import io.milton.annotations.Move;
import io.milton.annotations.Name;
import io.milton.annotations.PutChild;
import io.milton.annotations.ResourceController;
import io.milton.annotations.UniqueId;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

/**
 *
 * @author brad
 */
@ResourceController
public class AFolderController {
        
    @ChildrenOf
    public List<MyDatabase.AbstractContentItem> children(MyDatabase.FolderContentItem folder) {
        return folder.getChildren();
    }
    
    @MakeCollection
    public MyDatabase.FolderContentItem createFolder(MyDatabase.FolderContentItem parent, String name) {
        return parent.addFolder(name);
    }
    
    @PutChild
    public MyDatabase.FileContentItem createFile(MyDatabase.FolderContentItem parent, String name, byte[] bytes) {
        FileContentItem file = parent.addFile(name);
        file.setBytes(bytes);
        return file;
    }
    
    @Name
    public String getResourceName(MyDatabase.AbstractContentItem item) {
        return item.getName();
    }
    
    @Get
    public byte[] render(MyDatabase.FolderContentItem item) throws UnsupportedEncodingException {
        return "<html>\n<body><h1>hello world</h1></body></html>".getBytes("UTF-8");        
    }
    
    @Get
    public void writeContent(MyDatabase.FileContentItem item, OutputStream out) throws UnsupportedEncodingException, IOException {
        out.write(item.getBytes());
    }    
    
    @Move
    public void move(MyDatabase.AbstractContentItem source, MyDatabase.FolderContentItem newParent, String newName) {
        source.moveTo(newParent);
        source.setName(newName);
    }
    
    @Copy
    public void copy(MyDatabase.AbstractContentItem source, MyDatabase.FolderContentItem newParent, String newName) {
        source.copyTo(newParent, newName);
    }    
    
    @Delete
    public void delete(MyDatabase.AbstractContentItem source) {
        source.delete();
    }
    
    @DisplayName
    public String getDisplayName(MyDatabase.AbstractContentItem source) {
        return "Hello " + source.getName();
    }
    
    @UniqueId
    public String getUniqueId(MyDatabase.AbstractContentItem source) {
        return source.getId().toString();
    }    
    
    @ModifiedDate
    public Date getModifiedDate(MyDatabase.AbstractContentItem source) {
        return source.getModifiedDate();
    }
    
    @CreatedDate
    public Date getCreatedDate(MyDatabase.AbstractContentItem source) {
        return source.getCreatedDate();
    }    
}
