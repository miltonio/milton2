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

package io.milton.simpleton;

import io.milton.http.HttpManager;
import io.milton.http.http11.Http11ResponseHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bradm (zfc1502)
 */
public class SimpletonServer implements Container {

    private static final Logger log = LoggerFactory.getLogger(SimpletonServer.class);
    protected final Stage<Task> dispatchStage;
	private final HttpManager httpManager;
    private final Http11ResponseHandler responseHandler;
    private int httpPort = 80;
    private int sslPort = 0;
    private String certificatesDir;
    private Thread thMonitor;
    private boolean stopped;
    private int maxQueueTimeMillis = 10000;
    private int maxProcessTimeMillis = 60000;
    private Connection connection;

    public SimpletonServer(HttpManager httpManager, Http11ResponseHandler responseHandler, int capacity, int numThreads) {
		this.httpManager = httpManager;
        dispatchStage = new Stage<Task>("dispatchStage", capacity, numThreads, false);
        this.responseHandler = responseHandler;
        thMonitor = new Thread(new TaskMonitor());
    }


    public void start() {
        stopped = false;

        try {
            connection = new SocketConnection(this);
        } catch (Exception ex) {
            throw new RuntimeException("Couldnt create socket connection", ex);
        }

        initHttp(connection, httpPort);

        thMonitor = new Thread(new TaskMonitor());
        thMonitor.start();
    }

    protected void initHttp(Connection connection, int port) {
        SocketAddress address = new InetSocketAddress(port);
        try {
            connection.connect(address);
        } catch (java.net.BindException ex) {
            throw new RuntimeException("Couldnt bind to port: " + port);
        } catch (Exception ex) {
            throw new RuntimeException("Couldnt start connection", ex);
        }

        log.info("Simpleton server is now running on: " + address);
    }

    public void stop() {
        try {
            dispatchStage.close();
        } catch (IOException ex) {
            log.error("exception closing dispatchStage", ex);
        }
        stopped = true;
        thMonitor.interrupt();
        try {
            connection.close();
        } catch (Exception ex) {
            log.error("exception closing simpleton connection", ex);
        }
    }

    @Override
    public void handle(Request request, Response response) {
        Task task = new Task(httpManager, request, response);
        try {
            dispatchStage.enqueue(task);
        } catch (Exception e) {
            log.debug("exception dispatching request: " + e.getMessage());
            SimpleMiltonRequest req = new SimpleMiltonRequest(request);
            SimpleMiltonResponse resp = new SimpleMiltonResponse(response);
            respondError(req, resp, e.getMessage());
        }
    }

    public void respondError(SimpleMiltonRequest req, SimpleMiltonResponse resp, String reason) {
        responseHandler.respondServerError(req, resp, reason);
    }

    private void respondError(Task t) {
        try {
            log.warn("setting error status becaue request could not be processed");
            t.response.setCode(500);
//            t.response.commit();
            t.response.close();
        } catch (Exception e) {
            log.error("error setting last chance error status", e);
        }
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public int getSslPort() {
        return sslPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    public void setSslPort(int sslPort) {
        this.sslPort = sslPort;
    }

    public String getCertificatesDir() {
        return certificatesDir;
    }

    public void setCertificatesDir(String certificatesDir) {
        this.certificatesDir = certificatesDir;
    }

    public int getMaxProcessTimeMillis() {
        return maxProcessTimeMillis;
    }

    public void setMaxProcessTimeMillis(int maxProcessTimeMillis) {
        this.maxProcessTimeMillis = maxProcessTimeMillis;
    }

    public int getMaxQueueTimeMillis() {
        return maxQueueTimeMillis;
    }

    public void setMaxQueueTimeMillis(int maxQueueTimeMillis) {
        this.maxQueueTimeMillis = maxQueueTimeMillis;
    }

    public class TaskMonitor implements Runnable {

        public void run() {
            boolean isInterrupted = false;
            while (!stopped && !isInterrupted) {
                checkTasks();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    log.debug("interrupted");
                    isInterrupted = true;
                }
            }
        }
    }

    private void checkTasks() {
        long l;
        for (Task t : this.dispatchStage.queue) {
            // check enqueue time
            l = System.currentTimeMillis() - t.enqueueTime;
            if (l > maxQueueTimeMillis) {
                // bif it
                log.warn("XXX task is too long in queue: " + l + "ms. " + t);
                log.warn("Queue Size: " + dispatchStage.queue.size());
                log.warn("listing contents of queue -");
                for (Task q : dispatchStage.queue) {
                    log.warn(" - " + q.request.getTarget());
                }
                log.warn("---");
                this.dispatchStage.queue.remove(t);
                respondError(t);
            } else {
                if (t.startTime > 0) {
                    // check process time
                    l = System.currentTimeMillis() - t.startTime;
                    if (l > maxProcessTimeMillis) {
                        log.warn("**** task is too long being processed: " + l + "ms. " + t);
                        t.thisThread.interrupt();
                    }
                }
            }
        }
    }
}
