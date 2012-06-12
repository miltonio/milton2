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
