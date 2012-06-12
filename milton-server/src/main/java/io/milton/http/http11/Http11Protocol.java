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

package io.milton.http.http11;

import io.milton.http.Handler;
import io.milton.http.HandlerHelper;
import io.milton.http.HttpExtension;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author brad
 */
public class Http11Protocol implements HttpExtension{

    private final Set<Handler> handlers;

    private final HandlerHelper handlerHelper;

    private List<CustomPostHandler> customPostHandlers;

    public Http11Protocol( Set<Handler> handlers, HandlerHelper handlerHelper ) {
        this.handlers = handlers;
        this.handlerHelper = handlerHelper;
    }

    /**
     * OPTIONS authentication is disabled by default
     *
     * @param responseHandler
     * @param handlerHelper
     */
    public Http11Protocol(Http11ResponseHandler responseHandler, HandlerHelper handlerHelper) {
        this(responseHandler, handlerHelper, false );
    }

    public Http11Protocol(Http11ResponseHandler responseHandler, HandlerHelper handlerHelper, boolean enableOptionsAuth) {
        this.handlers = new HashSet<Handler>();
        this.handlerHelper = handlerHelper;
        handlers.add(new OptionsHandler(responseHandler, handlerHelper, enableOptionsAuth));
        handlers.add(new GetHandler(responseHandler, handlerHelper));
        handlers.add(new PostHandler(responseHandler, handlerHelper));
        handlers.add(new DeleteHandler(responseHandler, handlerHelper));
        handlers.add(new PutHandler(responseHandler, handlerHelper));
    }

    public Set<Handler> getHandlers() {
        return handlers;
    }

    public HandlerHelper getHandlerHelper() {
        return handlerHelper;
    }

    public List<CustomPostHandler> getCustomPostHandlers() {
        return customPostHandlers;
    }
}
