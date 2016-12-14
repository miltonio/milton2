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

package io.milton.grizzly;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.HttpManager;
import io.milton.http.ResourceFactory;
import io.milton.mail.MailServer;
import io.milton.mail.MailServerBuilder;
import java.io.IOException;
import java.security.Security;
import org.apache.commons.lang.StringUtils;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

/**
 * Use this as a runnable class to start a Grizzly HTTP Server
 *
 * On startup it will look for a spring config file in /applicationContext.xml ,
 * and then try to locate a bean named milton.http.manager which must be either
 * a milton HttpManager or HttpManagerBuilder
 *
 * You might find its easiest to copy this class into your app and then add
 * your own init
 *
 *
 *
 * @author brad
 */
public class GrizzlyServer {

	private static final Logger log = LoggerFactory.getLogger(GrizzlyServer.class);

	public static void main(String[] args) throws IOException, InterruptedException {
		int port = 8080;
		if( args.length > 0 ) {
			port = Integer.parseInt(args[0]);
		}
		Integer sslPort = null;
		if( args.length > 1) {
			sslPort = Integer.parseInt(args[1]);
		}
		GrizzlyServer k = new GrizzlyServer();
		k.start();
		System.out.println("Press any key to stop the server...");
		System.in.read();

	}

	private HttpServer httpServer;
	private StaticApplicationContext parent;
	private HttpManager httpManager;
	private MailServer mailServer;
	private MiltonSNIService kademiSNIService;
	private boolean running;

	public GrizzlyServer() {

	}

	public void start() throws IOException {
		start(8080);
	}

	public void start(int httpPort) throws IOException {
		start(httpPort, null);
	}

	public boolean  start(int httpPort, Integer sslPort) throws IOException {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        ConfigurableApplicationContext ctx = initSpringApplicationContext();
        if (ctx == null) {
            log.warn("Failed to initialise spring");
            return false;
        }

        Object milton = ctx.getBean("milton.http.manager");
        if (milton instanceof HttpManager) {
            this.httpManager = (HttpManager) milton;
        } else if (milton instanceof HttpManagerBuilder) {
            HttpManagerBuilder builder = (HttpManagerBuilder) milton;
            ResourceFactory rf = builder.getMainResourceFactory();
            this.httpManager = builder.buildHttpManager();
        }

        if (parent.containsBean("milton.mail.server")) {
            log.info("init mailserver...");
            Object oMailServer = parent.getBean("milton.mail.server");
            if (oMailServer instanceof MailServer) {
                mailServer = (MailServer) oMailServer;
            } else if (oMailServer instanceof MailServerBuilder) {
                MailServerBuilder builder = (MailServerBuilder) oMailServer;
                mailServer = builder.build();
            } else {
                throw new RuntimeException("Unsupported type: " + oMailServer.getClass() + " expected " + MailServer.class + " or " + MailServerBuilder.class);
            }
            log.info("starting mailserver");
            mailServer.start();
        }
        log.info("Finished init");

        String host = getPropertyOrDefault("host", null);

        int port = getPropertyOrDefaultInt("port", 8080);

        int secureHttpPort = getPropertyOrDefaultInt(MiltonSNIService.SYS_SECURE_PORT, MiltonSNIService.SECURE_PORT);

        if (host == null) {
            httpServer = HttpServer.createSimpleServer(null, port);
        } else {
            httpServer = HttpServer.createSimpleServer(null, host, port);
        }

        {   // Start the Kademi SNI SSL service
			MiltonSNICertificateStore store = null; // TODO: allow injection
            MiltonSNICertificateManager sniCerManager = new MiltonSNICertificateManager(store);
            SSLEngineConfigurator sniConfig = sniCerManager.createEngineConfigurator();
            this.kademiSNIService = new MiltonSNIService(secureHttpPort, sniConfig);

            this.kademiSNIService.startOn(httpServer);
        }

        httpServer.getServerConfiguration().addHttpHandler(
                new HttpHandler() {
                    @Override
                    public void service(Request request, Response response) throws Exception {
                        log.trace("service");
                        GrizzlyMiltonRequest req = new GrizzlyMiltonRequest(request);
                        GrizzlyMiltonResponse resp = new GrizzlyMiltonResponse(response);
                        String p = req.getAbsolutePath();
                        long tm = System.currentTimeMillis();
                        httpManager.process(req, resp);
                        tm = System.currentTimeMillis() - tm;
                        // todo
                    }
                },
                "/");

        httpServer.start();

        running = true;
        return true;
	}

	@SuppressWarnings("resource")
	protected ConfigurableApplicationContext initSpringApplicationContext() {

		log.info("No root spring context");
		parent = new StaticApplicationContext();

		ConfigurableApplicationContext ctx = null;
		String[] contextFiles = new String[]{"applicationContext.xml"};
		parent.refresh();
		try {
			ctx = new ClassPathXmlApplicationContext(contextFiles, parent);
		} catch (BeansException e) {
			log.error("Unable to create a child context for Milton", e);
		}
		return ctx;

	}


    private int getPropertyOrDefaultInt(String propSuffix, int defaultVal) {
        String name = "kademi." + propSuffix;
        String s = System.getProperty(name);
        if (StringUtils.isNotBlank(s)) {
            log.info("Using System property: " + name + " = " + s);
            return Integer.parseInt(s);
        }
        log.info("Using default value " + defaultVal + " for property " + name);
        return defaultVal;
    }

    public static String getPropertyOrDefault(String propSuffix, String defaultVal) {
        String name = "kademi." + propSuffix;
        String s = System.getProperty(name);
        if (StringUtils.isNotBlank(s)) {
            log.info("Using System property: " + name + " = " + s);
            return s;
        }
        log.info("Using default value " + defaultVal + " for property " + name);
        return defaultVal;
    }
}
