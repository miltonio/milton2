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

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.OneToMany;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author brad
 */
@javax.persistence.Entity
@DiscriminatorValue("M")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Musician extends BaseEntity {

    public static List<Musician> findAll(Session session) {
        Criteria crit = session.createCriteria(Musician.class);
        crit.addOrder(Order.asc("name"));
        return DbUtils.toList(crit, Musician.class);
    }

    public static Musician find(String newName, Session session) {
        Criteria crit = session.createCriteria(Musician.class);
        crit.add(Restrictions.eq("name", newName));
        return DbUtils.unique(crit);
    }

    public static Musician create(String name, Session session) {
        Musician m = new Musician();
        m.setName(name);
        m.setCreatedDate(new Date());
        m.setModifiedDate(new Date());
        SessionManager.session().save(m);
        return m;
    }

    
    private List<BandMember> bandMembers;
    private String avatarImageName; // image of the musician
    private String musicType; // eg singer, drummer, etc
    private String password; // to allow the user to login
    private String givenName;
    private String surName;
    private String mail;
    private String telephonenumber;
    private String contactUid;

    @Column(nullable = false)
    public String getContactUid() {
        return contactUid;
    }

    public void setContactUid(String contactUid) {
        this.contactUid = contactUid;
    }

    
    
    public String getAvatarImageName() {
        return avatarImageName;
    }

    public void setAvatarImageName(String avatarImageName) {
        this.avatarImageName = avatarImageName;
    }

    public String getMusicType() {
        return musicType;
    }

    public void setMusicType(String musicType) {
        this.musicType = musicType;
    }
    
    @OneToMany(mappedBy = "musician")
    public List<BandMember> getBandMembers() {
        return bandMembers;
    }

    public void setBandMembers(List<BandMember> bandMembers) {
        this.bandMembers = bandMembers;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    
    public void removeFromBand(Band band, Session session) {
        if( getBandMembers() != null ) {
            Iterator<BandMember> it = getBandMembers().iterator();
            while(it.hasNext()) {
                BandMember bm = it.next();
                if( bm.getBand() == band ) {
                    session.delete(bm);
                    it.remove();
                }
            }
        }
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getTelephonenumber() {
        return telephonenumber;
    }

    public void setTelephonenumber(String telephonenumber) {
        this.telephonenumber = telephonenumber;
    }
    
    
    
}
