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
package io.milton.ftp;

import io.milton.common.Path;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiltonFsView implements FileSystemView {

	private static final Logger log = LoggerFactory.getLogger(MiltonFsView.class);
	private final String host;
	private final Path homePath;
	private final ResourceFactory resourceFactory;
	private final MiltonUser user;
	private Path currentPath;

	public MiltonFsView(Path homePath, String host, ResourceFactory resourceFactory, MiltonUser user) {
		super();
		this.user = user;
		if (homePath.isRelative()) {
			throw new IllegalArgumentException("homePath must be absolute");
		}
		this.host = host;
		this.homePath = homePath;
		this.currentPath = homePath;
		this.resourceFactory = resourceFactory;
	}

	@Override
	public FtpFile getHomeDirectory() throws FtpException {
		try {
			Resource home = resourceFactory.getResource(host, homePath.toString());
			return wrap(homePath, home);
		} catch (NotAuthorizedException | BadRequestException ex) {
			throw new FtpException(ex);
		}

	}

	@Override
	public FtpFile getWorkingDirectory() throws FtpException {
		return wrap(currentPath, getWorkingDir());
	}

	@Override
	public boolean changeWorkingDirectory(String dir) throws FtpException {
		try {
			log.debug("cd: " + dir + " from " + currentPath);
			Path p = evaluateRelativePath(currentPath, dir);
			ResourceAndPath rp = getResource(p);
			if (rp.resource == null) {
				log.debug("not found: " + p);
				return false;
			} else if (rp.resource instanceof CollectionResource) {
				currentPath = rp.path;
				log.debug("currentPath is now: " + currentPath);
				return true;
			} else {
				log.debug("not a collection: " + rp.resource.getName());
				return false;
			}
		} catch (NotAuthorizedException | BadRequestException ex) {
			throw new FtpException(ex);
		}
	}

	@Override
	public FtpFile getFile(String path) throws FtpException {
		try {
			log.debug("getFile: " + path);
			if (path.startsWith(".")) {
				path = currentPath.toString() + path.substring(1);
				log.debug("getFile2: " + path);
			}
			Path p = Path.path(path);
			ResourceAndPath rp = getResource(p);
			if (rp.resource == null) {
				log.debug("returning new file");
				return new MiltonFtpFile(this, rp.path, getWorkingDir(), null, user);
			} else {
				return new MiltonFtpFile(this, rp.path, rp.resource, user);
			}
		} catch (NotAuthorizedException | BadRequestException ex) {
			throw new FtpException(ex);
		}
	}

	private CollectionResource getWorkingDir() throws FtpException {
		try {
			Resource working = resourceFactory.getResource(host, currentPath.toString());
			if (working instanceof CollectionResource) {
				return (CollectionResource) working;
			}
			return null;
		} catch (NotAuthorizedException | BadRequestException e) {
			throw new FtpException(e);
		}

	}

	@Override
	public boolean isRandomAccessible() throws FtpException {
		return true;
	}

	@Override
	public void dispose() {
	}

	public ResourceAndPath getResource(Path p) throws NotAuthorizedException, BadRequestException {
		log.debug("getResource: " + p);
		if (p.isRelative()) {
			p = Path.path(currentPath.toString() + '/' + p.toString());
			Resource r = resourceFactory.getResource(user.domain, p.toString());
			return new ResourceAndPath(r, p);
		} else {
			Resource r = resourceFactory.getResource(user.domain, p.toString());
			return new ResourceAndPath(r, p);
		}
	}

	public FtpFile wrap(Path path, Resource r) {
		return new MiltonFtpFile(this, path, r, user);
	}

	private Path evaluateRelativePath(Path currentPath, String dir) {
		Path changeTo = Path.path(dir);
		if (changeTo.isRelative()) {
			Path p = currentPath;
			for (String part : changeTo.getParts()) {
				if (part.equals("..")) {
					p = p.getParent();
				} else if (part.equals(".")) {
					p = p;
				} else {
					p = p.child(part);
				}
			}
			return p;
		} else {
			return changeTo;
		}
	}

	/**
	 * Represents a resource (possibly null) and an absolute path (never null)
	 */
	public static class ResourceAndPath {

		final Resource resource;
		final Path path;

		public ResourceAndPath(Resource r, Path p) {
			if (p == null) {
				throw new IllegalArgumentException("path may not be null");
			}
			if (p.isRelative()) {
				throw new IllegalArgumentException("path must be absolute");
			}
			this.resource = r;
			this.path = p;
		}
	}
}
