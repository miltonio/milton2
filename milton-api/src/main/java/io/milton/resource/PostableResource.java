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

import io.milton.http.FileItem;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import java.util.Map;

public interface PostableResource extends  GetableResource {
    
    /**
     * Called after a POST request
     * 
     * @param parameters
     * @param files
     * @return - null,or an address if a redirect is required.
     * @throws BadRequestException
     * @throws NotAuthorizedException
     * @throws ConflictException 
     */
    String processForm(Map<String,String> parameters, Map<String,FileItem> files) throws BadRequestException, NotAuthorizedException, ConflictException;
}
