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
import io.milton.common.Path;
import io.milton.http.HttpManager;
import io.milton.http.ResourceFactory;
import io.milton.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * Provides access to resources exposed by the servlet context.
 * <p>
 * Attempts to locate a physical file, via getRealPath. This will usually work,
 * but may not in cases where the webapp is running from a war file, or if
 * overlays are used.
 * <p>
 * If not found it attempts to locate a URL with servletContext.getResource
 *
 * @author brad
 */
public class WebResourceFactory implements ResourceFactory, Initable {

    private static final Logger log = LoggerFactory.getLogger(WebResourceFactory.class);

    private Config config;
    private File fileHome;
    private String basePath = "WEB-INF/static";
    private final Date modDate = new Date();

    public WebResourceFactory() {
    }

    public WebResourceFactory(Config config) {
        this.config = config;
    }

    public WebResourceFactory(File fileHome) {
        this.config = null;
        this.fileHome = fileHome;
        log.info("init fileHome={}", fileHome.getAbsoluteFile());
    }

    @Override
    public void init(Config config, HttpManager manager) {
        this.config = config;
    }

    @Override
    public Resource getResource(String host, String url) {
        Path p = Path.path(url);
        String contentType;
        if (config != null) {
            contentType = MiltonUtils.getContentType(config.getServletContext(), p.getName());
        } else {
            contentType = ContentTypeUtils.findContentTypes(p.getName());
        }

        File file;
        String path = stripContext(url);
        path = basePath + path;
        path = path.trim();

        // Reject obvious traversal/encoded traversal attempts before any path resolution
        if (path.contains("..") || path.contains("\\") || path.contains("%")) {
            log.error("getResource: Invalid path {}, rejected suspicious path characters", path);
            return null;
        }

        String realPath;
        if (config != null) {
            realPath = config.getServletContext().getRealPath(path);
        } else {
            realPath = fileHome.getAbsolutePath() + path;
        }
        file = toFile(realPath, path);
        if (file == null) {
            return null;
        }

        if (config != null && (file == null || !file.exists())) {
            URL resource;
            try {
                resource = config.getServletContext().getResource(path);
            } catch (MalformedURLException ex) {
                //throw new RuntimeException(ex);
                log.warn("malformed url when attempting to locate servlet resource {}", path);
                return null;
            }
            if (resource != null) {
                return new UrlResource(p.getName(), resource, contentType, modDate);
            }
            return null;
        } else {
            if (file.isFile()) {
                return new StaticResource(file);
            } else {
                return null;
            }
        }
    }

    @Override
    public void destroy(HttpManager manager) {
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    private String stripContext(String url) {
        if (config == null) {
            return url;
        }
        String contextName = config.getServletContext().getServletContextName();
        if (contextName == null || contextName.isEmpty() || config.getServletContext().getServletContextName().equals("/")) {
            return url;
        }
        String contextPath = "/" + contextName;
        url = url.replaceFirst('/' + contextPath, "");
        return url;
    }

    private File toFile(String realPath, String path) {
        File file;
        if (realPath != null) {
            file = new File(realPath);
            try {
                File baseDir;
                if (config != null) {
                    String baseRealPath = config.getServletContext().getRealPath(basePath);
                    baseDir = (baseRealPath == null) ? null : new File(baseRealPath);
                } else {
                    baseDir = fileHome;
                }
                if (baseDir != null) {
                    String baseCanonical = baseDir.getCanonicalPath();
                    String fileCanonical = file.getCanonicalPath();
                    if (!fileCanonical.equals(baseCanonical) && !fileCanonical.startsWith(baseCanonical + File.separator)) {
                        log.error("getResource: Invalid path {}, resolved outside base directory", path);
                        return null;
                    }
                }
            } catch (java.io.IOException ex) {
                log.warn("getResource: Failed canonical path validation for {}", path, ex);
                return null;
            }
        } else {
            file = null;
        }
        return file;
    }
}
