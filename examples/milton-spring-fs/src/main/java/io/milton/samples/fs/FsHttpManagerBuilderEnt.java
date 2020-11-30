package io.milton.samples.fs;

import io.milton.ent.config.HttpManagerBuilderEnt;
import io.milton.http.HttpExtension;
import io.milton.http.http11.Http11Protocol;
import io.milton.http.webdav.ResourceTypeHelper;
import io.milton.http.webdav.WebDavResponseHandler;

import java.util.ArrayList;

public class FsHttpManagerBuilderEnt extends HttpManagerBuilderEnt {
    protected void buildProtocolHandlers(WebDavResponseHandler webdavResponseHandler, ResourceTypeHelper resourceTypeHelper) {
        super.buildProtocolHandlers(webdavResponseHandler, resourceTypeHelper);
        final ArrayList<HttpExtension> protocols = getProtocols();
        if (protocols.removeIf(x -> x instanceof Http11Protocol)) {
            FsHttp11Protocol http11Protocol = new FsHttp11Protocol(webdavResponseHandler, handlerHelper, resourceHandlerHelper, enableOptionsAuth, matchHelper, partialGetHelper);
            protocols.add(http11Protocol);
        }
    }
}
