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

/** Passes the request and response along a series of filters
 *
 *  By default the HttpManager loads a single filter which executes the appropriate
 *  handler for the http method
 *
 *  Additional filters can be added using HttpManager.addFilter
 */
public class FilterChain {
    
    final HttpManager httpManager;
    int pos = 0;
    
    public FilterChain(HttpManager httpManager) {
        this.httpManager = httpManager;
    }

    public void process( Request request, Response response) {        
        Filter filter = httpManager.filters.get(pos++);
        filter.process(this,request,response);
    }

    public HttpManager getHttpManager() {
        return httpManager;
    }
       
}
