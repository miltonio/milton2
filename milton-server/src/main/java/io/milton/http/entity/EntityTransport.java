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

package io.milton.http.entity;

import io.milton.http.Response;

/**
 * Represents a means of writing entities to the HTTP response. For most
 * containers this is trivial, simply write to the output stream on the Responsee
 * 
 * However, some containers have an architecture where the content transmission can
 * be deferred, and this abstraction exists to support that.
 * 
 * For example, Restlet uses an API with deferred content transmission. But also
 * SEDA (http://www.eecs.harvard.edu/~mdw/proj/seda/) servers generally will want
 * to use a seperate thread pool for generating content from that which processes
 * request headers etc
 *
 * @author brad
 */
public interface EntityTransport {
	public void sendResponseEntity(Response response) throws Exception;

	public void closeResponse(Response response);
}
