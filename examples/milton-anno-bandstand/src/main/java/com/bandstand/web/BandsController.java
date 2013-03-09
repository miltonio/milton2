/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.bandstand.web;

import com.bandstand.domain.Band;
import com.bandstand.domain.BandMember;
import com.bandstand.domain.Gig;
import com.bandstand.domain.SessionManager;
import io.milton.annotations.ChildOf;
import io.milton.annotations.ChildrenOf;
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
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
@ResourceController
public class BandsController {

    private static final Logger log = LoggerFactory.getLogger(BandsController.class);
    
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
     * Instantiate but do not save a new Band object. For use in .new page with
     * web browser editing. Note used for webdav
     * 
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
        log.info("MOVE: band=" + band.getName() + " - to name=" + newName);
        Session session = SessionManager.session();
        Transaction tx = session.beginTransaction();
        band.setName(newName);
        session.save(band);
        session.flush();
        tx.commit();
        log.info("Saved band: " + band.getId());
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
