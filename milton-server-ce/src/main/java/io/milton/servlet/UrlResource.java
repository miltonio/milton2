/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.milton.servlet;

import io.milton.common.ContentTypeUtils;
import io.milton.http.Auth;
import io.milton.http.LockToken;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.resource.GetableResource;
import io.milton.resource.Resource;
import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author brad
 */
public class UrlResource implements GetableResource {
	private final String name;
	private final URL resource;
	private final String contentType;
	private final Date modDate;

	public UrlResource(String name, URL resource, String contentType, Date modDate) {
		this.resource = resource;
		this.name = name;
		this.contentType = contentType;
		this.modDate = modDate;
	}

	@Override
	public String getUniqueId() {
		return null;
	}

	public int compareTo(Resource res) {
		return this.getName().compareTo(res.getName());
	}

	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
		InputStream in = null;
		try {
			in = resource.openStream();
			BufferedInputStream bin = new BufferedInputStream(in);
			final byte[] buffer = new byte[1024];
			int n = 0;
			while (-1 != (n = bin.read(buffer))) {
				out.write(buffer, 0, n);
			}
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object authenticate(String user, String password) {
		return "ok";
	}

	@Override
	public boolean authorise(Request request, Request.Method method, Auth auth) {
		return true;
	}

	@Override
	public String getRealm() {
		return "milton.io"; // will never be used because authorise is always true
	}

	@Override
	public Date getModifiedDate() {
		return modDate;
	}

	@Override
	public Long getContentLength() {
		return null;
	}

	@Override
	public String getContentType(String preferredList) {
		return ContentTypeUtils.findAcceptableContentTypeForName(getName(), contentType);
	}

	@Override
	public String checkRedirect(Request request) {
		return null;
	}

	@Override
	public Long getMaxAgeSeconds(Auth auth) {
		Long ll = 315360000l; // immutable
		return ll;
	}
}
