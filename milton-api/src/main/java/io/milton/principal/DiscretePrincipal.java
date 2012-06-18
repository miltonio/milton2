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

package io.milton.principal;

import io.milton.resource.Resource;


/**
 * Indicates a principle which is identifiable by a URL, like a user or
 * an application defined group
 *
 * @author brad
 */
public interface DiscretePrincipal extends Principal, Resource{

        
    /**
     * A URL to identify this principle. Note the relationship between this and
	 * the AccessControlledResource.getPrincipalURL method which returns the principal
	 * that owns the resource.
	 * 
	 * It is assumed that where a AccessControlledResource instance is also a DiscretePrincipal
	 * that the getPrincipalURL method will return the url of the resource/principal
	 * 
	 * In other words, we make the semantic decision that a principle owns itself.
     *
     * @return
     */
    public String getPrincipalURL();


}
