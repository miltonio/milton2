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

package io.milton.http.json;

import io.milton.http.Range;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.GetableResource;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 *
 * @author brad
 */
public class AjaxLoginResource extends JsonResource implements GetableResource{

    private final String name;

    private final GetableResource wrapped;

    public AjaxLoginResource( String name, GetableResource wrapped ) {
        super(wrapped, name, null );
        this.name = name;
        this.wrapped = wrapped;
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        // nothing to send
    }

    @Override
    public Method applicableMethod() {
        return Method.GET;
    }

}
