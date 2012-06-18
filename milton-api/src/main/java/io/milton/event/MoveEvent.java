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

package io.milton.event;

import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;


/**
 * Fired just before the resource is moved.
 *
 * @author brad
 */
public class MoveEvent implements ResourceEvent{
    private final Resource res;
	private final CollectionResource destCollection;
	private final String newName;

	/**
	 * 
	 * @param res - the resource to move
	 * @param destCollection - the destination collection
	 * @param destNewName - the name of the resource within the destination folder
	 */
    public MoveEvent( Resource res, CollectionResource destCollection, String destNewName ) {
        this.res = res;
		this.destCollection = destCollection;
		this.newName = destNewName;
    }

    @Override
    public Resource getResource() {
        return res;
    }

	public CollectionResource getDestCollection() {
		return destCollection;
	}

	public String getNewName() {
		return newName;
	}
	
	

}
