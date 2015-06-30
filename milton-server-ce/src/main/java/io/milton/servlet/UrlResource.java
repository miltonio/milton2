/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
