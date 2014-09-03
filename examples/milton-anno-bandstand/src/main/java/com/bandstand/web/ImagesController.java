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
import com.bandstand.domain.Musician;
import com.bandstand.domain.SessionManager;
import io.milton.annotations.AccessControlList;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.ContentLength;
import io.milton.annotations.Delete;
import io.milton.annotations.Get;
import io.milton.annotations.Move;
import io.milton.annotations.Name;
import io.milton.annotations.PutChild;
import io.milton.annotations.ResourceController;
import io.milton.http.annotated.AnnoCollectionResource;
import io.milton.http.annotated.AnnoResource;
import io.milton.resource.AccessControlledResource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.hibernate.Transaction;

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
    
//    @AccessControlList
//    public List<AccessControlledResource.Priviledge> getPrivsOnImages(ImagesRoot target, Musician currentUser) {
//        System.out.println("getPrivsOnImages");
//        return AccessControlledResource.READ_WRITE;
//    }        

//    @AccessControlList
//    public List<AccessControlledResource.Priviledge> getPrivsOnBaseEntity(BaseEntity target, Musician currentUser) {
//        System.out.println("getPrivsOnBaseEntity");
//        return AccessControlledResource.READ_WRITE;
//    }        
    
    
    @ChildrenOf
    public ImagesRoot getImagesRoot(BandMember bandMember) {
        return new ImagesRoot(bandMember.getMusician()); // we want a band member folder to be same as a musician
    }    

    @ChildrenOf
    public List<Image> getImages(ImagesRoot imagesRoot) {
        return imagesRoot.getEntity().getImages();
    }

    @PutChild
    public Image uploadImage(ImagesRoot root, String newName, byte[] bytes, AnnoCollectionResource parentCol) throws IOException {
        System.out.println("upload. parent=" + parentCol);
        Transaction tx = SessionManager.session().beginTransaction();
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
        SessionManager.session().flush();
        tx.commit();
        System.out.println("added image: " + i.getDisplayName());
        return i;
    }
    
    /**
     *  Shows you can request a parent object in the arguments list, ie ImagesRoot
     */
    @PutChild
    public Image uploadImage(Image image, byte[] bytes, ImagesRoot root) throws IOException {
        System.out.println("replacing image in: " + root );
        File content = new File(image.getFileName());
        FileUtils.writeByteArrayToFile(content, bytes);
        return image;
    }    
        
    @Delete
    public void deleteImage(Image image) {
        Transaction tx = SessionManager.session().beginTransaction();
        try {
            SessionManager.session().delete(image);
            SessionManager.session().flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }        
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
    public void move(Image image, ImagesRoot newParentImagesRoot, String newName) {
        BaseEntity newParent = newParentImagesRoot.getEntity();
        System.out.println("Move image: " + image.getFileName() + " to " + newParent.getName());
        Transaction tx = SessionManager.session().beginTransaction();
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
        SessionManager.session().flush();
        tx.commit();
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
        File fRoot = new File("target" + File.separator + "files"); // folder to put these files
        if( !fRoot.exists() ) {
            if( !fRoot.mkdirs() ) {
                throw new RuntimeException("WARN: couldnt create directory to store file content - " + fRoot.getAbsolutePath());
            }
        }
        return fRoot;
    }
    
}
