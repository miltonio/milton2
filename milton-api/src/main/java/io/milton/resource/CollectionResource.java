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

package io.milton.resource;

import io.milton.resource.Resource;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import java.util.List;

/**
 * A type of Resource which can have children, ie it can act as a directory.
 * <P/>
 * This is only part of the normal behaviour of a directory though, you
 * should have a look at FolderResource for a more complete interface
 * 
 * @author brad
 */
public interface CollectionResource extends Resource {

    public Resource child(String childName) throws NotAuthorizedException, BadRequestException;
	
    List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException;
}
