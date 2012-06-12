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

package com.ettrema.examples.db.resources;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.SecurityManager;
import com.ettrema.examples.db.domain.Vehicle;
import com.ettrema.examples.db.domain.VehicleDao;
import com.ettrema.http.fs.LockManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class DemoDbResourceFactory implements ResourceFactory {

    private static final Logger log = LoggerFactory.getLogger( DemoDbResourceFactory.class );
    private final com.bradmcevoy.http.SecurityManager sm;
    private final LockManager lockManager;
    private final VehicleDao vehicleDao;

    public DemoDbResourceFactory( SecurityManager sm, LockManager lockManager, VehicleDao vehicleDao ) {
        this.sm = sm;
        this.lockManager = lockManager;
        this.vehicleDao = vehicleDao;
    }


    public Resource getResource( String host, String path ) {
        log.debug( "getResource: " + path );
        Path p = Path.path( path );
        log.debug( "length: " + p.getLength() );
        if( p.getLength() == 0 ) {
            return new AllMakes( this, sm );
        } else if( p.getLength() == 1 ) {
            if( p.getName().equals( "allvehicles.csv")) {
                return new AllVehiclesCsv( "allvehicles.csv", this, sm, vehicleDao);
            } else {
                return new VehicleMake( this, sm, p.getName() );
            }
        } else if( p.getLength() == 2 ) {
            VehicleMake makeResource = new VehicleMake( this, sm, p.getParent().getName() );
            return makeResource.child( p.getName());
        } else {
            return null;
        }
    }

    public List<Resource> getAllMakes() {
        Set<String> makes = vehicleDao.getAllVehicleMakes();
        List<Resource> list = new ArrayList<Resource>();
        for(String s : makes) {
            list.add( new VehicleMake( this, sm, s));
        }
        return list;
    }

    public List<? extends Resource> getAllMakesAndCsv() {
        List<Resource> list = getAllMakes();
        list.add( new AllVehiclesCsv( "allvehicles.csv", this, sm, vehicleDao));
        return list;
    }

    public List<VehicleResource> getVehiclesByMake(String make) {
        List<Vehicle> vehicles = vehicleDao.getVehiclesByMake(make);
        List<VehicleResource> list = new ArrayList<VehicleResource>();
        for( Vehicle v : vehicles ) {
            VehicleResource r = new VehicleResource( this, sm, v, vehicleDao );
            list.add( r );
        }
        return list;
    }
}
