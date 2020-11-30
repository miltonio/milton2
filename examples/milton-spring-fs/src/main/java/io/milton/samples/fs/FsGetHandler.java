package io.milton.samples.fs;

import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.ResourceHandlerHelper;
import io.milton.http.Response;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.fs.FsDirectoryResource;
import io.milton.http.http11.GetHandler;
import io.milton.http.http11.Http11ResponseHandler;
import io.milton.http.http11.MatchHelper;
import io.milton.http.http11.PartialGetHelper;
import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class FsGetHandler extends GetHandler {
    private static final Logger log = LoggerFactory.getLogger(GetHandler.class);
    private GetHandler previousHandler;
    private final DefaultResourceLoader loader = new DefaultResourceLoader();
    private static final String START_TIME = "" + System.currentTimeMillis();

    public FsGetHandler(Http11ResponseHandler responseHandler, ResourceHandlerHelper resourceHandlerHelper, MatchHelper matchHelper, PartialGetHelper partialGetHelper) {
        super(responseHandler, resourceHandlerHelper, matchHelper, partialGetHelper);
    }

    @Override
    public void processResource(HttpManager manager, Request request, Response response, Resource r) throws NotAuthorizedException, ConflictException, BadRequestException {
        try {
            final org.springframework.core.io.Resource resource = loader.getResource("classpath:handler/index.html");
            if (r instanceof FsDirectoryResource) {
                PrintStream stream = new PrintStream(response.getOutputStream(), true, StandardCharsets.UTF_8.toString());
                response.setContentTypeHeader("text/html");
                String handler = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
                handler = handler.replace("<%startTime%>", START_TIME);
                stream.println(handler);
                stream.flush();
            } else {
                previousHandler.processResource(manager, request, response, r);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    @Override
    public String[] getMethods() {
        return new String[]{Request.Method.GET.code, Request.Method.HEAD.code};
    }

    void setPreviousHandler(GetHandler resourceHandler) {
        previousHandler = resourceHandler;
    }
}
