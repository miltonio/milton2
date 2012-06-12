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
 * Thrown when there is an attempt to lock an already locked resource
 *
 * @author brad
 */
public class LockedException extends MiltonException {
    private static final long serialVersionUID = 1L;

    public LockedException(Resource r) {
        super(r);
    }

}
