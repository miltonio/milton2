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
package com.bandstand.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.*;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.criterion.Order;

/**
 *
 * @author brad
 */
@javax.persistence.Entity
@DiscriminatorValue("B")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Band extends BaseEntity {
    private List<Gig> gigs;
    
    public static List<Band> findAll(Session session) {
        Criteria crit = session.createCriteria(Band.class);
        crit.addOrder(Order.asc("name"));
        return DbUtils.toList(crit, Band.class);
    }       
        
    private List<Song> songs;
    private List<BandMember> bandMembers;
    
    
    @OneToMany(mappedBy = "band")
    public List<BandMember> getBandMembers() {
        return bandMembers;
    }

    public void setBandMembers(List<BandMember> bandMembers) {
        this.bandMembers = bandMembers;
    }

    @OneToMany(mappedBy = "band")
    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public BandMember addMember(Musician m, Session session) {
        if( getBandMembers() == null ) {
            setBandMembers(new ArrayList<BandMember>());
        }
        BandMember bm = new BandMember();
        bm.setBand(this);
        bm.setMusician(m);
        getBandMembers().add(bm);
        session.save(bm);
        return bm;
    }

    @OneToMany(mappedBy = "band")
    public List<Gig> getGigs() {
        return gigs;
    }

    public void setGigs(List<Gig> gigs) {
        this.gigs = gigs;
    }

    public Gig addGig(String title, Date dt) {
        if( getGigs() == null ) {
            setGigs(new ArrayList<Gig>());
        }
        Gig g = new Gig();
        g.setStartDate(dt);
        g.setBand(this);
        g.setDisplayName(title);
        g.setFileName(UUID.randomUUID().toString());
        g.setCreatedDate(new Date());
        g.setModifiedDate(new Date());
        getGigs().add(g);
        return g;
    }
    
}
