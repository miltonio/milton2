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

package io.milton.http.webdav;

import io.milton.property.PropertySource;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class PropertySourcesList extends ArrayList<PropertySource> {

	private static final Logger log = LoggerFactory.getLogger( PropertySourcesList.class );
	
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an empty list
     */
    public PropertySourcesList() {
		log.info("Created PropertySourcesList");
    }

	@Override
	public boolean add(PropertySource e) {
		log.info("adding property source: " + e.getClass() + " to PropertySourcesList: " + hashCode());
		return super.add(e);
	}

	
	
    /**
     * Allows you to add an extra source to the default list
     *
     * @param source
     */
    public void setExtraSource( PropertySource source ) {
        this.add( source );
    }

    public void setSources( List<PropertySource> sources ) {
        this.clear();
        this.addAll( sources );
    }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for( PropertySource l : this ) {
			sb.append(l.getClass()).append(",");
		}
		return sb.toString();
	}
	
	
}
