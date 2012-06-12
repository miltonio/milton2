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

package com.ettrema.examples.db.domain;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

/**
 *
 * @author brad
 */
public class VehicleDao {

    private final SessionFactory sessionFactory;
    private long nextId;

    public VehicleDao( SessionFactory sessionFactory ) {
        this.sessionFactory = sessionFactory;
    }

    public Set<String> getAllVehicleMakes() {
        Session sess = sessionFactory.openSession();
        Query query = sess.createQuery( "SELECT distinct  v.vehicleMake FROM Vehicle v" );
        List list = query.list();
        Set<String> makes = new LinkedHashSet<String>();
        for( Object o : list ) {
            String m = (String) o;
            makes.add( m );
        }
        return makes;
    }

    public void deleteAll() {
        Session sess = sessionFactory.openSession();
        Query query = sess.createQuery( "DELETE FROM Vehicle v" );
        query.executeUpdate();
    }

    public Vehicle add( Vehicle v ) {
        v.setId( nextId++ );
        Session sess = sessionFactory.openSession();
        Transaction t = sess.beginTransaction();
        sess.save( v );
        sess.flush();
        t.commit();


        return v;
    }

    public List<Vehicle> getAllVehicles() {
        Session sess = sessionFactory.openSession();
        List list = sess.createQuery( "SELECT v FROM Vehicle v" ).list();
        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        for( Object o : list ) {
            vehicles.add( (Vehicle) o );
        }
        return vehicles;
    }

    public List<Vehicle> getVehiclesByMake( String make ) {
        Session sess = sessionFactory.openSession();
        List list = sess.createQuery( "SELECT v FROM Vehicle v WHERE v.vehicleMake = '" + make + "'" ).list();
        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        for( Object o : list ) {
            vehicles.add( (Vehicle) o );
        }
        return vehicles;
    }

    public void update( Vehicle vehicle ) {
        Session sess = sessionFactory.openSession();
        Transaction t = sess.beginTransaction();
        sess.saveOrUpdate( vehicle );
        t.commit();
    }

    public void delete( Vehicle vehicle ) {
        Session sess = sessionFactory.openSession();
        Transaction t = sess.beginTransaction();
        sess.delete( vehicle);
        t.commit();
    }
}
