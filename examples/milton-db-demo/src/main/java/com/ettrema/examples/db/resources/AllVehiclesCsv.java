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

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.ReplaceableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.SecurityManager;
import com.ettrema.examples.db.domain.Vehicle;
import com.ettrema.examples.db.domain.VehicleDao;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class AllVehiclesCsv implements GetableResource, PropFindableResource, ReplaceableResource {

    private static final Logger log = LoggerFactory.getLogger( AllVehiclesCsv.class );

    private final String name;
    private final DemoDbResourceFactory resourceFactory;
    private final SecurityManager securityManager;
    private final VehicleDao vehicleDao;

    public AllVehiclesCsv( String name, DemoDbResourceFactory resourceFactory, SecurityManager securityManager, VehicleDao vehicleDao ) {
        this.name = name;
        this.resourceFactory = resourceFactory;
        this.securityManager = securityManager;
        this.vehicleDao = vehicleDao;
    }


    public Long getMaxAgeSeconds( Auth auth ) {
        return null;
    }

    public String getContentType( String accepts ) {
        return "text/csv";
    }

    public Long getContentLength() {
        return null;
    }

    public String getUniqueId() {
        return null;
    }

    public String getName() {
        return name;
    }

    public Object authenticate( String user, String password ) {
        return securityManager.authenticate( user, password );
    }

    public boolean authorise( Request request, Method method, Auth auth ) {
        return securityManager.authorise( request, method, auth, this );
    }

    public String getRealm() {
        return securityManager.getRealm( null );
    }

    public Date getModifiedDate() {
        return null;
    }

    public String checkRedirect( Request request ) {
        return null;
    }

    public Date getCreateDate() {
        return null;
    }

    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        PrintWriter pw = new PrintWriter(out);
        CSVWriter writer = new CSVWriter(pw);
        List<Vehicle> vehicles = vehicleDao.getAllVehicles();
        for( Vehicle v : vehicles ) {
            output(v,writer);
        }
        pw.flush();
        pw.close();
    }


    public void replaceContent( InputStream in, Long length ) {
        log.debug( "replaceContent");
        try {
            vehicleDao.deleteAll();
            InputStreamReader r = new InputStreamReader( in );
            CSVReader reader = new CSVReader( r );
            String[] line;
            int cnt = 0;
            while( ( line = reader.readNext() ) != null ) {
                if( line.length > 0 ) {
                    Vehicle v = new Vehicle();
                    log.debug( "make: " + line[0]);
                    v.setVehicleMake( line[0]);
                    v.setVehicleModel( line[1]);
                    Integer year = Integer.parseInt( line[2]);
                    v.setVehicleYear( year);
                    v.setRedBookReference( line[3]);
                    
                    vehicleDao.add(v);
                    log.debug( "processed: " + cnt++);
                }
            }
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
    }

    private void output( Vehicle v, CSVWriter writer ) {
        List<String> vals = new ArrayList<String>();
        vals.add(v.getVehicleMake());
        vals.add(v.getVehicleModel());
        vals.add(v.getVehicleYear().toString());
        vals.add(v.getRedBookReference());
        String[] arr = new String[vals.size()];
        vals.toArray(arr);
        writer.writeNext(arr);
    }
}
