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
import com.bandstand.domain.BandMember;
import com.bandstand.domain.Musician;
import com.bandstand.domain.SessionManager;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.Delete;
import io.milton.annotations.MakeCollection;
import io.milton.annotations.Move;
import io.milton.annotations.Name;
import io.milton.annotations.ResourceController;
import java.util.Date;
import java.util.List;
import org.hibernate.Transaction;

/**
 *
 * @author brad
 */
@ResourceController
public class BandsController {
    
    @ChildrenOf
    public BandsController getBandsRoot(RootController root) {
        return this;
    }    
        
    @ChildrenOf
    public List<Band> getBands(BandsController root) {
        return Band.findAll(SessionManager.session());
    }
    
    @Name
    public String getBandsRootName(BandsController bandsRoot) {
        return "bands";
    }
    
    
    @MakeCollection
    public Band createBand(BandsController root, String newName) {
        Band b = new  Band();
        b.setCreatedDate(new Date());
        b.setModifiedDate(new Date());
        b.setName(newName);
        SessionManager.session().save(b);
        return b;
    }
    
    @Move
    public void move(Band band, BandsController newParent, String newName) {
        Transaction tx = SessionManager.session().beginTransaction();
        band.setName(newName);
        SessionManager.session().save(band);
        tx.commit();
    }    
    
    @ChildrenOf
    public List<BandMember> getBandMembers(Band band) {
        return band.getBandMembers();
    }       

    @MakeCollection
    public BandMember createBandMember(Band band, String newName) {
        Transaction tx = SessionManager.session().beginTransaction();
        // Check for an existing musician with the given name, and create one if it doesnt exist
        Musician m = Musician.find(newName, SessionManager.session());
        if( m == null ) {
            m = Musician.create(newName, SessionManager.session());
        }
        BandMember bm = band.addMember(m, SessionManager.session()); // then add the musician to the band
        SessionManager.session().save(band);
        tx.commit();
        return bm;
    }    
    
    
    @Delete
    public void deleteBandMember(BandMember bm) {
        Transaction tx = SessionManager.session().beginTransaction();
        SessionManager.session().delete(bm);
        tx.commit();        
    }
    
    @Name
    public String getBandMemberName(BandMember bm) {
        return bm.getMusician().getName();
    }
}
