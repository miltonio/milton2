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

/**
 *
 * @author brad
 */
public class ProxyDetails {
    private boolean useSystemProxy;

    private String proxyHost;

    private int proxyPort;

    private String userName;

    private String password;

    /**
     * @return the useSystemProxy
     */
    public boolean isUseSystemProxy() {
        return useSystemProxy;
    }

    /**
     * @param useSystemProxy the useSystemProxy to set
     */
    public void setUseSystemProxy( boolean useSystemProxy ) {
        this.useSystemProxy = useSystemProxy;
    }

    /**
     * @return the proxyHost
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * @param proxyHost the proxyHost to set
     */
    public void setProxyHost( String proxyHost ) {
        this.proxyHost = proxyHost;
    }

    /**
     * @return the proxyPort
     */
    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * @param proxyPort the proxyPort to set
     */
    public void setProxyPort( int proxyPort ) {
        this.proxyPort = proxyPort;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword( String password ) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName( String userName ) {
        this.userName = userName;
    }

    public boolean hasAuth() {
        return (password != null && password.length() > 0 ) || (userName != null && userName.length() > 0);
    }
}
