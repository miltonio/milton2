/*
 * Copyright (C) 2012 McEvoy Software Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package io.milton.http.exceptions;

import io.milton.resource.Resource;

/**
 *  Indicates that the current user is not able to perform the requested operation
 *
 * This should not normally be used. Instead, a resource should determine if
 * a user can perform an operation in its authorised method
 *
 * However, this exception allows for cases where the authorised status can
 * only be determined during processing
 */
public class NotAuthorizedException extends MiltonException{
    private static final long serialVersionUID = 1L;

    public NotAuthorizedException(Resource r) {
        super(r);
    }

	public NotAuthorizedException(String message, Resource r) {
		super(message, r);
	}
	
	
	

}
