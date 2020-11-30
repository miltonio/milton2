package io.milton.samples.fs;

import io.milton.http.HandlerHelper;
import io.milton.http.ResourceHandlerHelper;
import io.milton.http.http11.*;

public class FsHttp11Protocol extends Http11Protocol {

    public FsHttp11Protocol(Http11ResponseHandler responseHandler, HandlerHelper handlerHelper, ResourceHandlerHelper resourceHandlerHelper, boolean enableOptionsAuth, MatchHelper matchHelper, PartialGetHelper partialGetHelper) {
        super(responseHandler, handlerHelper, resourceHandlerHelper, enableOptionsAuth, matchHelper, partialGetHelper);
        getHandlers().clear();
        getHandlers().add(new OptionsHandler(responseHandler, resourceHandlerHelper, handlerHelper, enableOptionsAuth));
        final GetHandler getHandler = new GetHandler(responseHandler, resourceHandlerHelper, matchHelper, partialGetHelper);
        final FsGetHandler fsGetHandler = new FsGetHandler(responseHandler, resourceHandlerHelper, matchHelper, partialGetHelper);
        fsGetHandler.setPreviousHandler(getHandler);
        getHandlers().add(fsGetHandler);
        getHandlers().add(new PostHandler(responseHandler, resourceHandlerHelper));
        getHandlers().add(new DeleteHandler(responseHandler, resourceHandlerHelper, handlerHelper));
        PutHelper putHelper = new PutHelper();
        getHandlers().add(new PutHandler(responseHandler, handlerHelper, putHelper, matchHelper));
    }
}
