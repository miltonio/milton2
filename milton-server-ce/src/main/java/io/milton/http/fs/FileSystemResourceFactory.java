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

import io.milton.common.Path;
import io.milton.http.LockManager;
import io.milton.http.ResourceFactory;
import io.milton.http.fs.NullSecurityManager;
import io.milton.resource.Resource;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource factory which provides access to files in a file system.
 *
 * Using this with milton is equivalent to using the dav servlet in tomcat
 *
 */
public final class FileSystemResourceFactory implements ResourceFactory {

    private static final Logger log = LoggerFactory.getLogger(FileSystemResourceFactory.class);
    private FileContentService contentService;
    File root;
    io.milton.http.SecurityManager securityManager;
    LockManager lockManager;
    Long maxAgeSeconds;
    String contextPath;
    boolean allowDirectoryBrowsing;
    String defaultPage;
    boolean digestAllowed = true;
    private String ssoPrefix;

    /**
     * Creates and (optionally) initialises the factory. This looks for a
     * properties file FileSystemResourceFactory.properties in the classpath If
     * one is found it uses the root and realm properties to initialise
     *
     * If not found the factory is initialised with the defaults root: user.home
     * system property realm: milton-fs-test
     *
     * These initialised values are not final, and may be changed through the
     * setters or init method
     *
     * To be honest its pretty naf configuring like this, but i don't want to
     * force people to use spring or any other particular configuration tool
     *
     */
    public FileSystemResourceFactory() {
        log.debug("setting default configuration...");
        String sRoot = System.getProperty("user.home");
        io.milton.http.SecurityManager sm = new NullSecurityManager();
        contentService = new SimpleFileContentService();
        init(sRoot, sm);
    }

    protected void init(String sRoot, io.milton.http.SecurityManager securityManager) {
        setRoot(new File(sRoot));
        setSecurityManager(securityManager);
    }

    /**
     *
     * @param root - the root folder of the filesystem to expose. This must
     * include the context path. Eg, if you've deployed to webdav-fs, root must
     * contain a folder called webdav-fs
     * @param securityManager
     */
    public FileSystemResourceFactory(File root, io.milton.http.SecurityManager securityManager) {
        setRoot(root);
        setSecurityManager(securityManager);
    }

    /**
     *
     * @param root - the root folder of the filesystem to expose. called
     * webdav-fs
     * @param securityManager
     * @param contextPath - this is the leading part of URL's to ignore. For
     * example if you're application is deployed to
     * http://localhost:8080/webdav-fs, the context path should be webdav-fs
     */
    public FileSystemResourceFactory(File root, io.milton.http.SecurityManager securityManager, String contextPath) {
        setRoot(root);
        setSecurityManager(securityManager);
        setContextPath(contextPath);
    }

    public File getRoot() {
        return root;
    }

    public final void setRoot(File root) {
        log.debug("root: " + root.getAbsolutePath());
        this.root = root;
        if (!root.exists()) {
            log.warn("Root folder does not exist: " + root.getAbsolutePath());
        }
        if (!root.isDirectory()) {
            log.warn("Root exists but is not a directory: " + root.getAbsolutePath());
        }
    }

    @Override
    public Resource getResource(String host, String url) {
        log.debug("getResource: host: " + host + " - url:" + url);
        url = stripContext(url);
        File requested = resolvePath(root, url);
        return resolveFile(host, requested);
    }


    public FsResource resolveFile(String host, File file) {
        FsResource r;
        if (!file.exists()) {
            log.debug("file not found: " + file.getAbsolutePath());
            return null;
        } else if (file.isDirectory()) {
            r = new FsDirectoryResource(host, this, file, contentService);
        } else {
            r = new FsFileResource(host, this, file, contentService);
        }
        if (r != null) {
            r.ssoPrefix = ssoPrefix;
        }
        return r;
    }

    public File resolvePath(File root, String url) {
        Path path = Path.path(url);
        File f = root;
        for (String s : path.getParts()) {
            f = new File(f, s);
        }
        return f;
    }

    public String getRealm(String host) {
        return securityManager.getRealm(host);
    }

    /**
     *
     * @return - the caching time for files
     */
    public Long maxAgeSeconds(FsResource resource) {
        return maxAgeSeconds;
    }

    public void setSecurityManager(io.milton.http.SecurityManager securityManager) {
        if (securityManager != null) {
            log.debug("securityManager: " + securityManager.getClass());
        } else {
            log.warn("Setting null FsSecurityManager. This WILL cause null pointer exceptions");
        }
        this.securityManager = securityManager;
    }

    public io.milton.http.SecurityManager getSecurityManager() {
        return securityManager;
    }

    public void setMaxAgeSeconds(Long maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;
    }

    public Long getMaxAgeSeconds() {
        return maxAgeSeconds;
    }

    public LockManager getLockManager() {
        return lockManager;
    }

    public void setLockManager(LockManager lockManager) {
        this.lockManager = lockManager;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getContextPath() {
        return contextPath;
    }

    /**
     * Whether to generate an index page.
     *
     * @return
     */
    public boolean isAllowDirectoryBrowsing() {
        return allowDirectoryBrowsing;
    }

    public void setAllowDirectoryBrowsing(boolean allowDirectoryBrowsing) {
        this.allowDirectoryBrowsing = allowDirectoryBrowsing;
    }

    /**
     * if provided GET requests to a folder will redirect to a page of this name
     * within the folder
     *
     * @return - E.g. index.html
     */
    public String getDefaultPage() {
        return defaultPage;
    }

    public void setDefaultPage(String defaultPage) {
        this.defaultPage = defaultPage;
    }

    private String stripContext(String url) {
        if (this.contextPath != null && contextPath.length() > 0) {
            url = url.replaceFirst('/' + contextPath, "");
            log.debug("stripped context: " + url);
            return url;
        } else {
            return url;
        }
    }

    boolean isDigestAllowed() {
        boolean b = digestAllowed && securityManager != null && securityManager.isDigestAllowed();
        if (log.isTraceEnabled()) {
            log.trace("isDigestAllowed: " + b);
        }
        return b;
    }

    public void setDigestAllowed(boolean digestAllowed) {
        this.digestAllowed = digestAllowed;
    }

    public void setSsoPrefix(String ssoPrefix) {
        this.ssoPrefix = ssoPrefix;
    }

    public String getSsoPrefix() {
        return ssoPrefix;
    }

    public FileContentService getContentService() {
        return contentService;
    }

    public void setContentService(FileContentService contentService) {
        this.contentService = contentService;
    }
}
