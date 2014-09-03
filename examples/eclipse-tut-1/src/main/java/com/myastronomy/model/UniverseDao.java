/*
 * Copyright 2012 McEvoy Software Ltd.
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
package com.myastronomy.model;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is to simulate the sort of relational/hierarchial data model typically
 * found in business applications.
 * 
 * This sort of data would normally be in a database or accessed over a webservice
 * but for simplicity of the tutorial we are holding it in memory
 *
 * @author brad
 */
public class UniverseDao {

    private final Set<Galaxy> galaxies = new HashSet<Galaxy>();

    public UniverseDao() {
        Galaxy g = addGalaxy("Milky way");
        SolarSystem ss = g.addSolarSystem("Sol");
        ss.addPlanet("Mercury");
        ss.addPlanet("Venus");
        ss.addPlanet("Earth");
        ss.addPlanet("Mars");
        ss.addPlanet("Jupiter");
        
        g = addGalaxy("Andromeda");
        g = addGalaxy("Bodes");                
    }

    public Galaxy addGalaxy(String name) {
        if( findGalaxy(name) != null  ) {
            throw new RuntimeException("Duplicate name exception");
        }
        Galaxy g = new Galaxy();
        g.setName(name);
        galaxies.add(g);
        return g;
    }
    
    public Set<Galaxy> getGalaxies() {
        return galaxies;
    }

    public Galaxy findGalaxy(String name) {
        for( Galaxy g : galaxies ) {
            if( g.getName().equals(name)) {
                return g;
            }
        }
        return null;
    }

    public class Galaxy {

        private String name;
        private final Set<SolarSystem> solarSystems = new HashSet<SolarSystem>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Set<SolarSystem> getSolarSystems() {
            return solarSystems;
        }
        
        public SolarSystem findSolarSystem(String name) {
            for( SolarSystem  s : solarSystems ) {
                if( s.getName().equals(name)) {
                    return s;
                }
            }
            return null;
        }
        
        public SolarSystem addSolarSystem(String name) {
            SolarSystem s = new SolarSystem();
            s.setName(name);
            solarSystems.add(s);
            return s;
        }
    }

    public class SolarSystem {

        private String name;
        private final Set<Planet> planets = new HashSet<Planet>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Set<Planet> getPlanets() {
            return planets;
        }
        
        public Planet findSolarSystem(String name) {
            for( Planet  s : planets ) {
                if( s.getName().equals(name)) {
                    return s;
                }
            }
            return null;
        }    
        
        public Planet addPlanet(String name) {
            Planet p = new Planet();
            p.setName(name);
            planets.add(p);
            return p;
        }
    }

    public class Planet {

        private String name;
        private int yearLength;
        private int radius;
        private String type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getYearLength() {
            return yearLength;
        }

        public void setYearLength(int yearLength) {
            this.yearLength = yearLength;
        }
    }
}
