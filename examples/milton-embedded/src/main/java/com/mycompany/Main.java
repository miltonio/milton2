/*
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
package com.mycompany;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.nio.*;
import org.eclipse.jetty.util.thread.*;
import org.eclipse.jetty.webapp.*;


/**
 *
 * @author brad
 */
public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("------------- START SERVER -----------------------");
        Main m = new Main(8080);
        m.start();
        System.out.println("---------------DONE ----------------------------------");
    }
    
    private static final String LOG_PATH = "./var/logs/access/yyyy_mm_dd.request.log";
    private static final String WEB_XML = "META-INF/webapp/WEB-INF/web.xml";
    private static final String PROJECT_RELATIVE_PATH_TO_WEBAPP = "src/main/webapp";

    public static interface WebContext {

        public File getWarPath();

        public String getContextPath();
    }

    private Server server;
    private int port;
    private String bindInterface;

    public Main(int aPort) {
        this(aPort, null);
    }

    public Main(int aPort, String aBindInterface) {
        port = aPort;
        bindInterface = aBindInterface;
    }

    public void start() throws Exception {
        server = new Server();

        server.setThreadPool(createThreadPool());
        server.addConnector(createConnector());
        server.setHandler(createHandlers());
        server.setStopAtShutdown(true);

        server.start();
    }

    public void join() throws InterruptedException {
        server.join();
    }

    public void stop() throws Exception {
        server.stop();
    }

    private ThreadPool createThreadPool() {
        // TODO: You should configure these appropriately
        // for your environment - this is an example only
        QueuedThreadPool _threadPool = new QueuedThreadPool();
        _threadPool.setMinThreads(10);
        _threadPool.setMaxThreads(100);
        return _threadPool;
    }

    private SelectChannelConnector createConnector() {
        SelectChannelConnector _connector
                = new SelectChannelConnector();
        _connector.setPort(port);
        _connector.setHost(bindInterface);
        return _connector;
    }

    private HandlerCollection createHandlers() {
        WebAppContext _ctx = new WebAppContext();
        _ctx.setContextPath("/");

        File f= new File(PROJECT_RELATIVE_PATH_TO_WEBAPP);
        System.out.println("dir: " + f.getAbsolutePath());
        _ctx.setWar(f.getAbsolutePath());

        List<Handler> _handlers = new ArrayList<Handler>();

        _handlers.add(_ctx);

        HandlerList _contexts = new HandlerList();
        _contexts.setHandlers(_handlers.toArray(new Handler[0]));

        RequestLogHandler _log = new RequestLogHandler();
        _log.setRequestLog(createRequestLog());

        HandlerCollection _result = new HandlerCollection();
        _result.setHandlers(new Handler[]{_contexts, _log});

        return _result;
    }

    private RequestLog createRequestLog() {
        NCSARequestLog _log = new NCSARequestLog();

        File _logPath = new File(LOG_PATH);
        _logPath.getParentFile().mkdirs();

        _log.setFilename(_logPath.getPath());
        _log.setRetainDays(90);
        _log.setExtended(false);
        _log.setAppend(true);
        _log.setLogTimeZone("GMT");
        _log.setLogLatency(true);
        return _log;
    }


    private URL getResource(String aResource) {
        return Thread.currentThread().getContextClassLoader().getResource(aResource);
    }

}
