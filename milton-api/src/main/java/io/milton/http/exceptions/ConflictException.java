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

package io.milton.http.exceptions;

import io.milton.resource.Resource;

/**
 *  Indicates that the requested operation could not be performed because of
 * prior state. Ie there is an existing resource preventing a new one from being
 * created.
 */
public class ConflictException extends MiltonException {

	private final String message;

	/**
	 * The resource idenfitied by the URI.
	 *
	 * @param r
	 */
	public ConflictException(Resource r) {
		super(r);
		this.message = "Conflict exception: " + r.getName();
	}

	public ConflictException(Resource r, String message) {
		super(r);
		this.message = message;
	}

	public ConflictException() {
		this.message = "Conflict";
	}

	public ConflictException(String message) {
		this.message = message;
	}
	
	

	@Override
	public String getMessage() {
		return message;
	}
}
