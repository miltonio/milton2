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

package io.milton.http.fs;

import io.milton.http.ResourceFactory;
import io.milton.resource.DigestResource;
import io.milton.resource.PostableResource;
import io.milton.http.Range;
import io.milton.resource.GetableResource;
import io.milton.common.ContentTypeUtils;
import io.milton.common.Path;
import io.milton.http.*;
import io.milton.http.Request.Method;
import io.milton.resource.Resource;
import io.milton.http.SecurityManager;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.http11.auth.DigestResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * A resource factory which looks for resources on the classpath
 *
 * @author brad
 */
public class ClassPathResourceFactory implements ResourceFactory {

	private static final Logger log = LoggerFactory.getLogger(ClassPathResourceFactory.class);
	private String basePath;
	private Long maxAgeSeconds = 60 * 60 * 24 * 7l;
	private io.milton.http.SecurityManager securityManager;
	private Date modifiedDate = new Date();

	@Override
	public Resource getResource(String host, String path) {
		Path p = Path.path(path);
		if (basePath != null) {
			if (p.getFirst().equals(basePath)) {
				p = p.getStripFirst();
			} else {
				return null;
			}
		}

		// try to locate a resource with the given path
		InputStream content = this.getClass().getResourceAsStream(p.toString());
		if (content == null) {
			return null;
		} else {
			log.trace("return class path resource");
			return new ClassPathResource(host, p, content);
		}
	}

	/**
	 *  The resource factory will only serve resources with a path which
	 * begins with this
	 *
	 * May be null
	 * 
	 * Eg static
	 */
	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	/**
	 * Sets a constant max age for all resources served by this resource factory
	 *
	 * @return
	 */
	public Long getMaxAgeSeconds() {
		return maxAgeSeconds;
	}

	public void setMaxAgeSeconds(Long maxAgeSeconds) {
		this.maxAgeSeconds = maxAgeSeconds;
	}

	/**
	 * Modified date for all content. May be null
	 *
	 * @return
	 */
	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	/**
	 * Security manager to delegate authentication and authorisation to. May
	 * be null
	 *
	 * @return
	 */
	public SecurityManager getSecurityManager() {
		return securityManager;
	}

	public void setSecurityManager(SecurityManager securityManager) {
		this.securityManager = securityManager;
	}

	public class ClassPathResource implements GetableResource, DigestResource, PostableResource {

		private final String host;
		private final Path path;
		private final InputStream content;

		public ClassPathResource(String host, Path path, InputStream content) {
			this.host = host;
			this.path = path;
			this.content = content;
			if (content == null) {
				throw new IllegalArgumentException("content cannot be null");
			}
		}

		public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
			try {
				IOUtils.copy(content, out);
			} catch (NullPointerException npe) {
				log.debug("NullPointerException, this is often expected");
			}
		}

		public Long getMaxAgeSeconds(Auth auth) {
			return maxAgeSeconds;
		}

		public String getContentType(String preferredList) {
			String mime = ContentTypeUtils.findContentTypes(path.getName());
			String s = ContentTypeUtils.findAcceptableContentType(mime, preferredList);
			if (log.isTraceEnabled()) {
				log.trace("getContentType: preferred: {} mime: {} selected: {}", new Object[]{preferredList, mime, s});
			}
			return s;
		}

		public Long getContentLength() {
			return null;
		}

		public String getUniqueId() {
			return null;
		}

		public String getName() {
			return path.getName();
		}

		public Object authenticate(String user, String password) {
			if (securityManager != null) {
				return securityManager.authenticate(user, password);
			} else {
				return "ok";
			}
		}

		public boolean authorise(Request request, Method method, Auth auth) {
			if (securityManager != null) {
				return securityManager.authorise(request, method, auth, this);
			} else {
				return true;
			}

		}

		public String getRealm() {
			if (securityManager != null) {
				return securityManager.getRealm(host);
			} else {
				return host;
			}

		}

		public Date getModifiedDate() {
			return modifiedDate;
		}

		public String checkRedirect(Request request) {
			return null;
		}

		public Object authenticate(DigestResponse digestRequest) {
			if (securityManager != null) {
				return securityManager.authenticate(digestRequest);
			} else {
				return false;
			}
		}

		public boolean isDigestAllowed() {
			return true;
		}

		public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws BadRequestException, NotAuthorizedException, ConflictException {
			return null;
		}
	}
}
