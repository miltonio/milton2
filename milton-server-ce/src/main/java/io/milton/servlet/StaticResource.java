/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.milton.servlet;

import io.milton.common.ContentTypeUtils;
import io.milton.common.RangeUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import io.milton.http.Auth;
import io.milton.http.LockToken;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.resource.GetableResource;
import io.milton.resource.Resource;
import org.apache.commons.io.IOUtils;

/**
 * Used to provide access to static files via Milton
 *
 * For a full implementation of webdav on a filesystem use the milton-filesysten
 * project
 *
 * @author brad
 */
public class StaticResource implements GetableResource {

	private final File file;

	public StaticResource(File file) {
		if (file.isDirectory()) {
			throw new IllegalArgumentException("Static resource must be a file, this is a directory: " + file.getAbsolutePath());
		}
		this.file = file;
	}

	@Override
	public String getUniqueId() {
		return file.getName() + "_ " + file.lastModified();
	}

	public int compareTo(Resource res) {
		return this.getName().compareTo(res.getName());
	}

	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			BufferedInputStream bin = new BufferedInputStream(fis);
			RangeUtils.writeRange(bin, range, out);
			IOUtils.closeQuietly(bin);
			out.flush();
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}

	@Override
	public String getName() {
		return file.getName();
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
		Date dt = new Date(file.lastModified());
//        log.debug("static resource modified: " + dt);
		return dt;
	}

	@Override
	public Long getContentLength() {
		return file.length();
	}

	@Override
	public String getContentType(String preferredList) {
		String mime = ContentTypeUtils.findContentTypes(getName());
		String s = ContentTypeUtils.findAcceptableContentType(mime, preferredList);
		return s;
	}

	@Override
	public String checkRedirect(Request request) {
		return null;
	}

	@Override
	public Long getMaxAgeSeconds(Auth auth) {
		//Long ll = 315360000l; // immutable
		Long ll = 25920000l; // 1 year
		return ll;
	}

	public LockToken getLockToken() {
		return null;
	}
}
