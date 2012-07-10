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
import io.milton.http.ResourceHandlerHelper;
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

    public Http11Protocol(Http11ResponseHandler responseHandler, HandlerHelper handlerHelper, ResourceHandlerHelper resourceHandlerHelper, boolean enableOptionsAuth, MatchHelper matchHelper, PartialGetHelper partialGetHelper) {
        this.handlers = new HashSet<Handler>();
        this.handlerHelper = handlerHelper;
        handlers.add(new OptionsHandler(responseHandler, resourceHandlerHelper, handlerHelper, enableOptionsAuth));
        handlers.add(new GetHandler(responseHandler, resourceHandlerHelper, matchHelper, partialGetHelper));
        handlers.add(new PostHandler(responseHandler, resourceHandlerHelper));
        handlers.add(new DeleteHandler(responseHandler, resourceHandlerHelper, handlerHelper));
		PutHelper putHelper = new PutHelper();
        handlers.add(new PutHandler(responseHandler, handlerHelper, putHelper, matchHelper));
    }

	@Override
    public Set<Handler> getHandlers() {
        return handlers;
    }

    public HandlerHelper getHandlerHelper() {
        return handlerHelper;
    }

	@Override
    public List<CustomPostHandler> getCustomPostHandlers() {
        return customPostHandlers;
    }
}
