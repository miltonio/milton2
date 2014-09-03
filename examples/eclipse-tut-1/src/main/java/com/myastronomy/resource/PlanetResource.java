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
package com.myastronomy.resource;

import com.myastronomy.model.UniverseDao.Planet;
import io.milton.http.Auth;
import io.milton.http.Range;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.CopyableResource;
import io.milton.resource.DeletableResource;
import io.milton.resource.GetableResource;
import io.milton.resource.MoveableResource;
import io.milton.resource.ReplaceableResource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Holds binary files like PDFs, jpeg, etc
 *
 * Demonstrates implementing CustomPropertyResource
 *
 * @author brad
 */
public class PlanetResource extends AbstractResource implements GetableResource, ReplaceableResource, MoveableResource, CopyableResource, DeletableResource{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PlanetResource.class);
    private final SolarSystemResource parent;
    private final Planet planet;

    public PlanetResource(SolarSystemResource parent, Planet planet) {
        this.parent = parent;
        this.planet = planet;
    }

    @Override
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        parent.getSolarSystem().getPlanets().remove(planet);
    }

    
    
    @Override
    public void copyTo(CollectionResource toCollection, String name) throws NotAuthorizedException, BadRequestException, ConflictException {
        if( toCollection instanceof SolarSystemResource ) {
            throw new BadRequestException("Can only copy planet to a SolarSystemResource folder. Current parent=" +parent.getName() + " dest parent=" + toCollection.getName());
        }
        SolarSystemResource newSolarSystem = (SolarSystemResource) toCollection;
        Planet pNew = newSolarSystem.getSolarSystem().addPlanet(name);
        pNew.setRadius(planet.getRadius());
        pNew.setType(planet.getType());
        pNew.setYearLength(planet.getYearLength());        
    }

    
    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
        Properties props = new Properties();
        if( planet.getType() == null ) {
            props.setProperty("type", "");
        } else {
            props.setProperty("type", planet.getType());
        }
        props.setProperty("radius", planet.getRadius() + "" );
        props.setProperty("yearLength", planet.getYearLength() + "" );
        props.store(out, null);
    }

    @Override
    public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
        Properties props = new Properties();
        try {
            props.load(in);
            planet.setType(props.getProperty("type"));
            planet.setRadius(asInt(props.getProperty("radius")));
            planet.setYearLength(asInt(props.getProperty("yearLength")));
        } catch (IOException ex) {
            throw new BadRequestException("Couldnt read properties", ex);
        }        
    }    
    
    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public String getContentType(String accept) {
        return "text/plain";
    }

    @Override
    public String getName() {
        return planet.getName();
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    private int asInt(String s) {
        if( s == null ) {
            return 0;
        }
        s = s.trim();
        if( s.length() == 0 ) {
            return 0;
        }
        return Integer.parseInt(s);
    }

    @Override
    public void moveTo(CollectionResource rDest, String newName) throws ConflictException, NotAuthorizedException, BadRequestException {
        if( rDest != parent ) {
            throw new BadRequestException("Cant move planet to a different folder. Current parent=" +parent.getName() + " dest parent=" + rDest.getName());
        }
        planet.setName(newName);
    }
}
