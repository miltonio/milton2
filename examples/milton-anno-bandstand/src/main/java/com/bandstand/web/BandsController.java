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
import com.bandstand.domain.Gig;
import com.bandstand.domain.Musician;
import com.bandstand.domain.SessionManager;
import io.milton.annotations.ChildOf;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.Delete;
import io.milton.annotations.DisplayName;
import io.milton.annotations.Get;
import io.milton.annotations.MakeCollection;
import io.milton.annotations.Move;
import io.milton.annotations.Name;
import io.milton.annotations.Post;
import io.milton.annotations.ResourceController;
import io.milton.common.JsonResult;
import io.milton.common.ModelAndView;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hibernate.Transaction;

/**
 *
 * @author brad
 */
@ResourceController
public class BandsController {

    @Get
    public ModelAndView renderBandPage(Band band) throws UnsupportedEncodingException {
        return new ModelAndView("band", band, "bandPage");
    }

    @Get(params = {"editMode"})
    public ModelAndView renderBandEditPage(Band band) throws UnsupportedEncodingException {
        return new ModelAndView("band", band, "bandEditPage");
    }

    @Get(contentType = "application/json")
    public JsonResult renderBandJson(Band band) throws UnsupportedEncodingException {
        return JsonResult.returnData(band);
    }

    @Post(bindData = true)
    public Band saveBand(Band band) {
        Transaction tx = SessionManager.session().beginTransaction();
        SessionManager.session().save(band);
        if (band.getGigs() == null) {
            for (int i = 0; i < 10; i++) {
                Date dt = new Date(System.currentTimeMillis() + i * 1000 * 60 * 60 * 24 * 2);
                Gig g = band.addGig("Test " + i, dt);
                SessionManager.session().save(g);
            }
        }
        tx.commit();
        return band;
    }

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
        Transaction tx = SessionManager.session().beginTransaction();
        Band b = new Band();
        b.setCreatedDate(new Date());
        b.setModifiedDate(new Date());
        b.setName(newName);
        SessionManager.session().save(b);

        for (int i = 0; i < 10; i++) {
            Date dt = new Date(System.currentTimeMillis() + i * 1000 * 60 * 60 * 24 * 2);
            Gig g = b.addGig("Test " + i, dt);
        }
        SessionManager.session().save(b);
        System.out.println(" band gigs: " + b.getGigs().size());
        SessionManager.session().save(b);
        tx.commit();
        return b;
    }

    /**
     * Instantiate but do not save a new Band object. For use in .new page
     * @param root
     * @return 
     */
    @ChildOf(pathSuffix = "new")
    public Band newBand(BandsController root) {
        Band b = new Band();
        b.setCreatedDate(new Date());
        b.setModifiedDate(new Date());

        return b;
    }
    
    @Move
    public void move(Band band, BandsController newParent, String newName) {
        Transaction tx = SessionManager.session().beginTransaction();
        band.setName(newName);
        SessionManager.session().save(band);
        tx.commit();
    }

    @Move
    public void move(BandMember bandMember, Band newParent, String newName) {
        Transaction tx = SessionManager.session().beginTransaction();
        bandMember.getMusician().setName(newName);
        bandMember.getBand().getBandMembers().remove(bandMember);
        bandMember.setBand(newParent);
        if (newParent.getBandMembers() == null) {
            newParent.setBandMembers(new ArrayList<BandMember>());
        }
        newParent.getBandMembers().add(bandMember);
        SessionManager.session().save(bandMember);
        tx.commit();
    }
    @DisplayName
    public String getBandDisplayName(Band band) {
        return band.getName();
    }
}
