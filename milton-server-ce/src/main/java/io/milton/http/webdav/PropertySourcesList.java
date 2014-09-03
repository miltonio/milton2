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
