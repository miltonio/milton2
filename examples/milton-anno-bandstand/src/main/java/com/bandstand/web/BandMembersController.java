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
import io.milton.annotations.ChildOf;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.Delete;
import io.milton.annotations.Get;
import io.milton.annotations.MakeCollection;
import io.milton.annotations.Name;
import io.milton.annotations.Post;
import io.milton.annotations.ResourceController;
import io.milton.common.ModelAndView;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
@ResourceController
public class BandMembersController {

    private static final Logger log = LoggerFactory.getLogger(BandMembersController.class);
    

    @ChildrenOf
    public List<BandMember> getBandMembers(Band band) {
        return band.getBandMembers();
    }

    @ChildOf(pathSuffix="newMember")
    public BandMember newMember(Band band) {
        BandMember bm = new BandMember();
        bm.setBand(band);
        if( band.getBandMembers() == null ) {
            band.setBandMembers(new ArrayList<BandMember>());
        }
        band.getBandMembers().add(bm);
        return bm;
    }
            
    @MakeCollection
    public BandMember createBandMember(Band band, String newName) {
        Transaction tx = SessionManager.session().beginTransaction();
        // Check for an existing musician with the given name, and create one if it doesnt exist
        Musician m = Musician.find(newName, SessionManager.session());
        if (m == null) {
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

    @Get
    public ModelAndView renderBandMemberPage(BandMember bm) throws UnsupportedEncodingException {
        ModelAndView mv = new ModelAndView("member", bm, "bandMemberPage");
        return mv;
    }

    @Post
    public BandMember saveBand(BandMember member, Map<String,String> params) {
        Transaction tx = SessionManager.session().beginTransaction();
        Musician m = Musician.find(params.get("musician"), SessionManager.session());
        member.setMusician(m);
        SessionManager.session().save(member);
        tx.commit();
        return member;
    }    
}
