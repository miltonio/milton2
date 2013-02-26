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

import com.bandstand.domain.Band;
import com.bandstand.domain.Image;
import com.bandstand.domain.SessionManager;
import com.bandstand.domain.Song;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.ContentLength;
import io.milton.annotations.Delete;
import io.milton.annotations.Get;
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
import org.hibernate.Transaction;

/**
 *
 * @author brad
 */
@ResourceController
public class SongsController {
    
    @ChildrenOf
    public SongsRoot getSongsRoot(Band entity) {
        return new SongsRoot(entity);
    }
    
    @ChildrenOf
    public List<Song> getSongs(SongsRoot songsRoot) {
        return songsRoot.getEntity().getSongs();
    }

    @PutChild
    public Song uploadSong(SongsRoot root, String newName, byte[] bytes) throws IOException {
        File fRoot = getContentRoot();
        File content = new File(fRoot, UUID.randomUUID().toString());
        FileUtils.writeByteArrayToFile(content, bytes);
        
        Song song = new Song();
        song.setDisplayName(newName);
        song.setFileName(content.getAbsolutePath());
        song.setBand(root.getEntity());
        if( root.getEntity().getImages() == null ) {
            root.getEntity().setImages(new ArrayList<Image>());
        }
        root.getEntity().getSongs().add(song);
        SessionManager.session().save(song);
        SessionManager.session().save(root.getEntity());
        return song;
    }
    
    @PutChild
    public Song uploadSong(Song song, byte[] bytes) throws IOException {
        File content = new File(song.getFileName());
        FileUtils.writeByteArrayToFile(content, bytes);
        return song;
    }    
        
    @Delete
    public void deleteSong(Song song) {
        Transaction tx = SessionManager.session().beginTransaction();
        try {
            SessionManager.session().delete(song);
            
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }        
    }
    @Get
    public InputStream getSongFile(Song song) throws IOException {
        File content = new File(song.getFileName());
        return FileUtils.openInputStream(content);                        
    }
    
    @ContentLength
    public Long getContentLength(Song song) {
        File content = new File(song.getFileName());
        return content.length();
    }
    
    @Name
    public String getSongName(Song song) {
        return song.getDisplayName();
    }
        
    @Delete
    public void pretendToDeleteImagesFolder(SongsRoot imagesFolder) {
        // is called when deleting the parent, in some cases. We just ignore it
    }
    
    public class SongsRoot {
        private final Band entity;

        public SongsRoot(Band entity) {
            this.entity = entity;
        }

        public Band getEntity() {
            return entity;
        }    
        
        public String getName() {
            return "songs";
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
