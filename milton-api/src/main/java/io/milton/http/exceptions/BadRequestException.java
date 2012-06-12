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
 *
 * @author brad
 */
public class BadRequestException extends MiltonException {
    private static final long serialVersionUID = 1L;

    private final String reason;

    public BadRequestException(Resource r) {
        super(r);
        this.reason = null;
    }

    public BadRequestException(Resource r, String reason) {
        super(r);
        this.reason = reason;
    }
	
    public BadRequestException(String reason) {
        super();
        this.reason = reason;
    }	
	
    public BadRequestException(String reason, Throwable cause) {
        super(cause); 
        this.reason = reason;
    }		

    /**
     * Optional property, which describe the cause of the exception
     * @return
     */
    public String getReason() {
        return reason;
    }



}
