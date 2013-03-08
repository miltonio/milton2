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

package io.milton.restlet.test;

import io.milton.http.fs.FileSystemResourceFactory;
import io.milton.http.fs.FsMemoryLockManager;
import io.milton.http.fs.SimpleFileContentService;
import io.milton.restlet.WebDavRestlet;
import io.milton.http.fs.NullSecurityManager;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;

import java.io.File;

public class RestletFileserver extends Component {

    public static void main(String[] args) throws Exception {
        Component component = new Component();

        component.getServers().add(Protocol.HTTP, 8080);

        // Restlet logs requests by default
        // component.getLogService().setEnabled(false);

        component.getDefaultHost().attach(
                new Application() {
                    @Override
                    public Restlet createInboundRoot() {

                        FileSystemResourceFactory factory = new FileSystemResourceFactory(
                                new File(System.getProperty("user.home")),
                                new NullSecurityManager()
                        );

                        factory.setContentService(new SimpleFileContentService());
                        factory.setLockManager(new FsMemoryLockManager());

                        return new WebDavRestlet(factory);
                    }
                }

        );

        component.start();

        System.out.println(
                "Restlet demo fileserver started, open http://localhost:8080 in your browser or WebDAV client..."
        );
    }


}
