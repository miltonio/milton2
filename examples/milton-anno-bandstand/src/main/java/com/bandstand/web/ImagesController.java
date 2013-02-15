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
package com.bandstand.web;

import com.bandstand.domain.BandMember;
import com.bandstand.domain.BaseEntity;
import com.bandstand.domain.Image;
import com.bandstand.domain.SessionManager;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.ContentLength;
import io.milton.annotations.Delete;
import io.milton.annotations.Get;
import io.milton.annotations.Move;
import io.milton.annotations.Name;
import io.milton.annotations.PutChild;
import io.milton.annotations.ResourceController;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author brad
 */
@ResourceController
public class ImagesController {
    
    @ChildrenOf
    public ImagesRoot getImagesRoot(BaseEntity entity) {
        return new ImagesRoot(entity);
    }
    
    @ChildrenOf
    public ImagesRoot getImagesRoot(BandMember bandMember) {
        return new ImagesRoot(bandMember.getMusician()); // we want a band member folder to be same as a musician
    }    

    @ChildrenOf
    public List<Image> getImages(ImagesRoot imagesRoot) {
        return imagesRoot.getEntity().getImages();
    }

    @PutChild
    public Image uploadImage(ImagesRoot root, String newName, byte[] bytes) throws IOException {
        File fRoot = getContentRoot();
        File content = new File(fRoot, UUID.randomUUID().toString());
        FileUtils.writeByteArrayToFile(content, bytes);
        
        Image i = new Image();
        i.setDisplayName(newName);
        i.setFileName(content.getAbsolutePath());
        i.setBaseEntity(root.getEntity());
        if( root.getEntity().getImages() == null ) {
            root.getEntity().setImages(new ArrayList<Image>());
        }
        root.getEntity().getImages().add(i);
        SessionManager.session().save(i);
        SessionManager.session().save(root.getEntity());
        System.out.println("added image: " + i.getDisplayName());
        return i;
    }
        
    @Get
    public InputStream getImageFile(Image image) throws IOException {
        File content = new File(image.getFileName());
        return FileUtils.openInputStream(content);                        
    }
    
    @ContentLength
    public Long getContentLength(Image image) {
        File content = new File(image.getFileName());
        return content.length();
    }
    
    @Name
    public String getImageName(Image image) {
        return image.getDisplayName();
    }
    
    @Move
    public void move(Image image, BaseEntity newParent, String newName) {
        if( newParent != image.getBaseEntity()) {
            BaseEntity oldParent = image.getBaseEntity();
            image.setBaseEntity(newParent);
            oldParent.getImages().remove(image);
            if( newParent.getImages() == null ) {
                newParent.setImages(new ArrayList<Image>());
            }
        }
        image.setDisplayName(newName);
        SessionManager.session().save(image);
    }
    
    @Delete
    public void pretendToDeleteImagesFolder(ImagesRoot imagesFolder) {
        // is called when deleting the parent, in some cases. We just ignore it
    }
    
    public class ImagesRoot {
        private final BaseEntity entity;

        public ImagesRoot(BaseEntity entity) {
            this.entity = entity;
        }

        public BaseEntity getEntity() {
            return entity;
        }    
        
        public String getName() {
            return "images";
        }
    }
    
    
    private File getContentRoot() {
        File fRoot = new File("target/files"); // folder to put these files
        if( !fRoot.exists() ) {
            if( !fRoot.mkdirs() ) {
                throw new RuntimeException("WARN: couldnt create directory to store file content - " + fRoot.getAbsolutePath());
            }
        }
        return fRoot;
    }
    
}
