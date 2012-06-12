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

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.examples.db.domain.Vehicle;
import com.ettrema.examples.db.domain.VehicleDao;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class VehicleResource implements PropFindableResource, GetableResource, PostableResource, DeletableResource {

    private static final Logger log = LoggerFactory.getLogger( VehicleResource.class );

    private final DemoDbResourceFactory resourceFactory;
    private final com.bradmcevoy.http.SecurityManager securityManager;
    private final Vehicle vehicle;
    private final VehicleDao vehicleDao;

    public VehicleResource( DemoDbResourceFactory resourceFactory, SecurityManager securityManager, Vehicle vehicle, VehicleDao vehicleDao ) {
        this.resourceFactory = resourceFactory;
        this.securityManager = securityManager;
        this.vehicle = vehicle;
        this.vehicleDao = vehicleDao;
    }


    public Date getCreateDate() {
        return null;
    }

    public String getUniqueId() {
        return vehicle.getId().toString();
    }

    public String getName() {
        return vehicle.getVehicleModel() + "_" + vehicle.getVehicleYear() + ".html";
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

    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        PrintWriter pw = new PrintWriter( out );
        pw.print( "<html>" );
        pw.print( "<body>" );
        pw.print( "<form method='POST'>" );
        pw.print( "Make: <input type='text' name='make' value='" + vehicle.getVehicleMake() + "'/><br/>" );
        pw.print( "Model: <input type='text' name='model' value='" + vehicle.getVehicleModel() + "'/><br/>" );
        pw.print( "Year: <input type='text' name='year' value='" + vehicle.getVehicleYear() + "'/><br/>" );
        pw.print( "Redbook Ref: <input type='text' name='redbook' value='" + vehicle.getRedBookReference() + "'/><br/>" );
        pw.print( "<input type='submit' value='save'/>");
        pw.print( "</form>" );
        pw.print( "</body>" );
        pw.print( "</html>" );
        pw.flush();
        pw.close();
    }

    public Long getMaxAgeSeconds( Auth auth ) {
        return null;
    }

    public String getContentType( String accepts ) {
        return "text/html";
    }

    public Long getContentLength() {
        return null;
    }

    public String processForm( Map<String, String> parameters, Map<String, FileItem> files ) throws BadRequestException, NotAuthorizedException {
        log.debug( "processForm: " + parameters.size());
        vehicle.setVehicleMake( parameters.get( "make" ) );
        vehicle.setVehicleModel( parameters.get( "model" ) );
        int year = Integer.parseInt( parameters.get( "year" ) );
        vehicle.setVehicleYear( year );
        vehicle.setRedBookReference( parameters.get( "redbook" ) );
        vehicleDao.update(vehicle);
        return null;

    }

    public void delete() {
        vehicleDao.delete(this.vehicle);
    }
}
