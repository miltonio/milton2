/*
 * Copyright 2013 McEvoy Software Ltd.
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
package io.milton.davproxy.adapter;

import io.milton.config.HttpManagerBuilder;
import io.milton.davproxy.content.FolderHtmlContentGenerator;
import io.milton.http.HttpManager;
import io.milton.http.fs.SimpleSecurityManager;
import io.milton.http.http11.auth.DigestGenerator;
import io.milton.httpclient.HostBuilder;
import io.milton.servlet.Config;
import io.milton.servlet.MiltonConfigurator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 * @author brad
 */
public class DavProxyConfigurator implements MiltonConfigurator{

    private final HttpManagerBuilder builder = new HttpManagerBuilder();
    
    @Override
    public HttpManager configure(Config config) throws ServletException {
        String realm = config.getInitParameter("realm");
        String userName = config.getInitParameter("userName");
        String password = config.getInitParameter("password");
        Map<String,String> userNamesAndPasswords = new HashMap<String, String>();
        userNamesAndPasswords.put(userName, password);
        FolderHtmlContentGenerator cg = new FolderHtmlContentGenerator();
        SimpleSecurityManager securityManager = new SimpleSecurityManager(realm, userNamesAndPasswords);
        securityManager.setDigestGenerator(new DigestGenerator());
        RemoteManager remoteManager = new RemoteManager(securityManager, cg);        
        
        Map<String, HostBuilder> davRoots = new HashMap<String, HostBuilder>();
        for( String s : config.getInitParameterNames()) {
            if( s.startsWith("server.")) {
                String serverConnect = config.getInitParameter(s);
                try {
                    URL url = new URL(serverConnect);
                    HostBuilder hb = configRemoteHost(url);
                    String serverId = s.replace("server.", "");
                    davRoots.put(serverId, hb);
                } catch (MalformedURLException malformedURLException) {
                    throw new RuntimeException("Couldnt configure remote DAV host: " + serverConnect, malformedURLException);
                }
            }
        }
        
        RemoteDavResourceFactory remoteDavResourceFactory = new RemoteDavResourceFactory(securityManager, cg, remoteManager, davRoots);
        builder.setMainResourceFactory(remoteDavResourceFactory);
        builder.setEnableFormAuth(false);
        builder.setEnableCookieAuth(false);
        builder.setEnabledCkBrowser(false);
        builder.setEnabledJson(false);
        //builder.setEnableBasicAuth(false);
        
        initBuilder();
        
        return builder.buildHttpManager();
    }

    protected void initBuilder() {
        builder.init();
    }
    
    @Override
    public void shutdown() {

    }

    /**
     * 
     * @param serverConnect
     * @return 
     */
    private HostBuilder configRemoteHost(URL url)  {        
        HostBuilder hb = new HostBuilder();
        hb.setServer(url.getHost());
        hb.setRootPath(url.getPath());
        hb.setSecure(url.getProtocol().equals("https"));
        hb.setPort(url.getPort());
        String userInfo = url.getUserInfo();
        if( userInfo.contains(":")) {
            String[] arr = userInfo.split(":");
            hb.setUser(arr[0]);
            hb.setPassword(arr[1]);
                    
        } else {
            hb.setUser(userInfo);
        }        
        return hb;
    }
    
}
