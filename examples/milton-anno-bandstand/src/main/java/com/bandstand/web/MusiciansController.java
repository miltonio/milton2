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
import com.bandstand.domain.Gig;
import com.bandstand.domain.Musician;
import com.bandstand.domain.SessionManager;
import io.milton.annotations.AccessControlList;
import io.milton.annotations.Authenticate;
import io.milton.annotations.ChildOf;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.Delete;
import io.milton.annotations.Get;
import io.milton.annotations.MakeCollection;
import io.milton.annotations.Move;
import io.milton.annotations.Name;
import io.milton.annotations.Post;
import io.milton.annotations.PutChild;
import io.milton.annotations.ResourceController;
import io.milton.annotations.Users;
import io.milton.common.JsonResult;
import io.milton.common.ModelAndView;
import io.milton.resource.AccessControlledResource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import net.fortuna.ical4j.data.ParserException;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
@ResourceController
public class MusiciansController {

    private static final Logger log = LoggerFactory.getLogger(MusiciansController.class);
    
    @ChildrenOf
    public MusiciansController getMusiciansRoot(RootController root) {
        return this;
    }

    @ChildrenOf
    @Users // ties in with the @AccessControlList and @Authenticate methods below
    public List<Musician> getMusicians(MusiciansController root) {
        List<Musician> list = Musician.findAll(SessionManager.session());
        System.out.println("musicians=" + list.size());
        return list;
    }
    
    @ChildOf
    @Users
    public Musician findMusicianByName(MusiciansController root, String name) {
        return Musician.find(name, SessionManager.session());
    }
            
    
    @Get(contentType="application/json")
    public JsonResult renderMusicianJson(Musician musician) throws UnsupportedEncodingException {
        return JsonResult.returnData(musician);
    }  
    
    @Get
    public ModelAndView renderMusicianPage(Musician musician) throws UnsupportedEncodingException {
        return new ModelAndView("musician", musician, "musicianPage"); 
    }    

    @Post(bindData=true)
    public Musician saveMusician(Musician musician) {
        log.info("saveMusician: " + musician.getName());
        Transaction tx = SessionManager.session().beginTransaction();
        if( musician.getContactUid() == null ) {
            musician.setContactUid(UUID.randomUUID().toString());
        }
        musician.setModifiedDate(new Date());
        SessionManager.session().save(musician);
        SessionManager.session().flush();
        tx.commit();
        log.info("saved musician");
        return musician;
    }
    
    @Get(params={"editMode"})
    public ModelAndView renderMusicianEditPage(Musician musician) throws UnsupportedEncodingException {
        return new ModelAndView("musician", musician, "musicianEditPage"); 
    }        
    
    @Authenticate
    public String getMusicianPassword(Musician m) {
        return m.getPassword(); // The @Authenticate also allows methods which verify a password and return Boolean
    }
    
    @AccessControlList
    public List<AccessControlledResource.Priviledge> getMusicianPrivs(Musician target, Musician currentUser) {
        if( target == currentUser ) {
            return AccessControlledResource.READ_WRITE;
        } else {
            // This prevents read access to each others calendars
            return null;
            
            // This gives read access to each others calendars
            //return AccessControlledResource.READ_CONTENT;
        }
    }
    
    @Name
    public String getMusiciansRootName(MusiciansController musiciansRoot) {
        return "musicians";
    }

    @Move
    public void move(Musician musician, MusiciansController newParent, String newName) {
        Transaction tx = SessionManager.session().beginTransaction();
        musician.setName(newName);
        if( musician.getContactUid() == null ) {
            musician.setContactUid(UUID.randomUUID().toString());
        }
        SessionManager.session().save(musician);
        SessionManager.session().flush();
        tx.commit();
    }

    @MakeCollection
    public Musician createAndSaveMusician(MusiciansController root, String newName) {
        Transaction tx = SessionManager.session().beginTransaction();
        Musician m = new Musician();
        m.setCreatedDate(new Date());
        m.setModifiedDate(new Date());
        m.setName(newName);
        SessionManager.session().save(m);
        SessionManager.session().flush();
        tx.commit();
        return m;
    }
    
    @ChildOf(pathSuffix="new")
    public Musician createNewMusician(MusiciansController root) {
        Musician m = new Musician();
        m.setCreatedDate(new Date());
        m.setModifiedDate(new Date());
        return m;
    }    
    
    @Delete
    public void deleteMusician(Musician musician) {
        Transaction tx = SessionManager.session().beginTransaction();
        if( musician.getBandMembers() != null ) {
            for( BandMember bm : musician.getBandMembers()) {
                SessionManager.session().delete(bm);
            }
        }
        SessionManager.session().delete(musician);
        SessionManager.session().flush();
        tx.commit();        
    }
    
    @PutChild
    public Gig uploadSomething(Musician m, String name, byte[] arr) throws IOException, ParserException {
        throw new RuntimeException("Not really supported");
    }    
}
