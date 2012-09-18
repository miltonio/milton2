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

import io.milton.http.*;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.GetableResource;
import io.milton.resource.PostableResource;
import io.milton.resource.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import net.sf.json.JSONObject;

/**
 *
 * @author brad
 */
public class AjaxLoginResource extends JsonResource implements GetableResource, PostableResource{

    public AjaxLoginResource( String name, Resource wrapped ) {
        super(wrapped, name, null );
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
		JSONObject json = new JSONObject();
		Request request = HttpManager.request();
		Boolean loginResult = (Boolean) request.getAttributes().get("loginResult");
		json.accumulate("loginResult", loginResult);
		String userUrl = (String) request.getAttributes().get("userUrl");
		if (userUrl != null) {
			json.accumulate("userUrl", userUrl);
		}
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(bout);
		json.write(pw);
		pw.flush();
		byte[] arr = bout.toByteArray();
		try {
			out.write(arr);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
    }

    @Override
    public Method applicableMethod() {
        return Method.GET;
    }

	@Override
	public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws BadRequestException, NotAuthorizedException, ConflictException {
		return null;
	}

}
