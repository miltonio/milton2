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

package io.milton.restlet;

import io.milton.http.entity.EntityTransport;


/**
 * Don't write the entity during Milton's call handling, let Restlet take care of that later.
 */
public class RestletEntityTransport implements EntityTransport {
    @Override
    public void sendResponseEntity(io.milton.http.Response r) throws Exception {
        // Take the Response.Entity from Milton and turn it into a Restlet Representation
        ((ResponseAdapter) r).setTargetEntity();
    }

    @Override
    public void closeResponse(io.milton.http.Response response) {
        // Restlet flushes later automatically
    }
}
