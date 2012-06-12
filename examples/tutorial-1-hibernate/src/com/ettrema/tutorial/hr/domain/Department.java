/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ettrema.tutorial.hr.domain;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Department implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static long nextId;
    
    private Long id;
    
    private String name;
     
    public static Department create(String name) {
    	Department d = new Department();
    	d.setId(nextId++);
    	d.setName(name);
    	return d;
    }


    @Column(length = 15)
    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    @Id
    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

}
