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
 *  Base class for exceptions during processing requests on resources
 */
public abstract class MiltonException extends Exception {
    private static final long serialVersionUID = 1L;
    private Resource resource;

	public MiltonException() {
	}
    public MiltonException(String message, Resource resource) {
		super(message);
        this.resource = resource;
    }
	
    public MiltonException(Resource resource) {
        this.resource = resource;
    }
	public MiltonException(String message) {
		super(message);
	}
	
	public MiltonException(Throwable cause) {
		super(cause);
	}	

    public Resource getResource() {
        return resource;
    }
    
}
