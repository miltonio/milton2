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
import com.bandstand.domain.Musician;
import com.bandstand.domain.SessionManager;
import io.milton.annotations.Authenticate;
import io.milton.annotations.ChildOf;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.Delete;
import io.milton.annotations.MakeCollection;
import io.milton.annotations.Move;
import io.milton.annotations.Name;
import io.milton.annotations.ResourceController;
import io.milton.annotations.Users;
import java.util.Date;
import java.util.List;
import org.hibernate.Transaction;

/**
 *
 * @author brad
 */
@ResourceController
public class MusiciansController {

    @ChildrenOf
    public MusiciansController getMusiciansRoot(RootController root) {
        return this;
    }

    @ChildrenOf
    @Users // ties in with the @Authenticate method below
    public List<Musician> getMusicians(MusiciansController root) {
        return Musician.findAll(SessionManager.session());
    }

    @Authenticate
    public String getMusicianPassword(Musician m) {
        return m.getPassword(); // The @Authenticate also allows methods which verify a password and return Boolean
    }
    
    @ChildOf
    public Musician findMusicianByName(MusiciansController root, String name) {
        return Musician.find(name, SessionManager.session());
    }
        
    @Name
    public String getMusiciansRootName(MusiciansController musiciansRoot) {
        return "musicians";
    }

    @Move
    public void move(Musician musician, MusiciansController newParent, String newName) {
        Transaction tx = SessionManager.session().beginTransaction();
        musician.setName(newName);
        SessionManager.session().save(musician);
        tx.commit();
    }

    @MakeCollection
    public Musician createMusician(MusiciansController root, String newName) {
        Transaction tx = SessionManager.session().beginTransaction();
        Musician m = new Musician();
        m.setCreatedDate(new Date());
        m.setModifiedDate(new Date());
        m.setName(newName);
        SessionManager.session().save(m);
        tx.commit();
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
        tx.commit();        
    }
}
