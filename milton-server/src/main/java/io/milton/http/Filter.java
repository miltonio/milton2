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

package io.milton.http;

/** Interface for a request/response wrapping filter.
 *  <P/>
 *  Add these with HttpManager.addFilter(ordinal,filter)
 *  <P/>
 *  By default the manager loads a single filter which delegates the
 *  request to a handler appropriate for the request method
 *  <P/>
 *  Users can add their own for logging, security, managing database connections etc
 *
 */
public interface Filter {
    public void process(FilterChain chain, Request request, Response response);   
}
