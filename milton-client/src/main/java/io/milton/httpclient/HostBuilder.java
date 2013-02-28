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

package io.milton.httpclient;

import com.ettrema.cache.Cache;
import io.milton.httpclient.zsyncclient.FileSyncer;
import java.util.List;

/**
 *
 * @author brad
 */
public class HostBuilder {
    private String server;
    private int port;
    private String user;
    private String password;
    private String rootPath;
    private ProxyDetails proxy;
    private Cache<Folder, List<Resource>> cache;
    private int timeoutMillis;
    private FileSyncer fileSyncer;
    private boolean secure = false;
    
    public Host buildHost() {
        Host h = new Host(server, rootPath, port, user, password, proxy, timeoutMillis, cache, fileSyncer);
        h.setSecure(secure);
        return h;
    }

    /**
     * @return the server
     */
    public String getServer() {
        return server;
    }

    /**
     * @param server the server to set
     */
    public void setServer(String server) {
        this.server = server;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isSecure() {
        return secure;
    }

    
    
    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the rootPath
     */
    public String getRootPath() {
        return rootPath;
    }

    /**
     * @param rootPath the rootPath to set
     */
    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public ProxyDetails getProxy() {
        return proxy;
    }

    public void setProxy(ProxyDetails proxy) {
        this.proxy = proxy;
    }

    public Cache<Folder, List<Resource>> getCache() {
        return cache;
    }

    public void setCache(Cache<Folder, List<Resource>> cache) {
        this.cache = cache;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public FileSyncer getFileSyncer() {
        return fileSyncer;
    }

    public void setFileSyncer(FileSyncer fileSyncer) {
        this.fileSyncer = fileSyncer;
    }

    
}
