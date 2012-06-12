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

package io.milton.davproxy.adapter;

import io.milton.common.Path;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.common.LogUtils;
import io.milton.davproxy.content.FolderHtmlContentGenerator;
import io.milton.httpclient.Host;
import io.milton.httpclient.HostBuilder;
import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author brad
 */
public class RemoteDavResourceFactory implements ResourceFactory {

    private static final Logger log = LoggerFactory.getLogger(RemoteDavResourceFactory.class);
    private final io.milton.http.SecurityManager securityManager;
    private final FolderHtmlContentGenerator contentGenerator;
    private final RemoteManager remoteManager;
    private final Map<String,Host> roots;

    public RemoteDavResourceFactory(io.milton.http.SecurityManager securityManager, FolderHtmlContentGenerator contentGenerator,RemoteManager remoteManager, Map<String,HostBuilder> roots) {
        this.securityManager = securityManager;
        this.contentGenerator = contentGenerator;
        this.remoteManager = remoteManager;
        this.roots = new ConcurrentHashMap();
        for( Entry<String, HostBuilder> entry : roots.entrySet()) {
            this.roots.put(entry.getKey(), entry.getValue().buildHost());
        }
    }

    @Override
    public Resource getResource(String host, String path) throws NotAuthorizedException, BadRequestException {
        LogUtils.trace(log, "getResource: path:", path);
        Path p = Path.path(path);
        return find(host, p);
    }

    /**
     * Recursive method which walks the parts of the path resolving it to a
     * Resource by using the child method on CollectionResource
     *
     * @param p
     * @return
     */
    private Resource find(String host, Path p) throws NotAuthorizedException, BadRequestException {
        if (p.isRoot()) {
            return new RootFolder(host, roots, this, contentGenerator, securityManager, remoteManager);
        } else {
            Resource rParent = find(host, p.getParent());
            if (rParent == null) {
                return null;
            } else {
                if (rParent instanceof CollectionResource) {
                    CollectionResource parent = (CollectionResource) rParent;
                    return parent.child(p.getName());
                } else {
                    return null;
                }
            }
        }
    }
           
}
