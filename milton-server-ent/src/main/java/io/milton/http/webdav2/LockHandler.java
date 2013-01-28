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
package io.milton.http.webdav2;

import io.milton.http.HttpManager;
import io.milton.resource.Resource;
import io.milton.resource.LockingCollectionResource;
import io.milton.resource.LockableResource;
import io.milton.http.LockResult;
import io.milton.http.ResourceHandler;
import io.milton.http.LockToken;
import io.milton.http.HandlerHelper;
import io.milton.http.LockTimeout;
import io.milton.http.LockInfo;
import io.milton.http.XmlWriter;
import io.milton.common.Path;
import io.milton.http.Request.Method;
import io.milton.http.Response.Status;
import io.milton.http.entity.ByteArrayEntity;
import io.milton.common.LogUtils;
import io.milton.http.*;
import io.milton.http.exceptions.*;
import io.milton.http.webdav.PropFindPropertyBuilder;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.http.webdav.WebDavResponseHandler;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import sun.security.provider.MD5;

/**
 * Note that this is both a new entity handler and an existing entity handler
 *
 * @author brad
 */
public class LockHandler implements ResourceHandler {

    private static final Logger log = LoggerFactory.getLogger(LockHandler.class);
    private final WebDavResponseHandler responseHandler;
    private final HandlerHelper handlerHelper;
    private LockWriterHelper lockWriterHelper;

    public LockHandler(WebDavResponseHandler responseHandler, HandlerHelper handlerHelper) {
        this.responseHandler = responseHandler;
        this.handlerHelper = handlerHelper;
        lockWriterHelper = new LockWriterHelper();
        displayCopyrightNotice();
    }

    public LockWriterHelper getLockWriterHelper() {
        return lockWriterHelper;
    }

    public void setLockWriterHelper(LockWriterHelper lockWriterHelper) {
        this.lockWriterHelper = lockWriterHelper;
    }

    @Override
    public void processResource(HttpManager manager, Request request, Response response, Resource r) throws NotAuthorizedException, ConflictException, BadRequestException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getMethods() {
        return new String[]{Method.LOCK.code};
    }

    @Override
    public void process(HttpManager manager, Request request, Response response) throws NotAuthorizedException, BadRequestException {
        if (!handlerHelper.checkExpects(responseHandler, request, response)) {
            return;
        }

        String host = request.getHostHeader();
        String url = HttpManager.decodeUrl(request.getAbsolutePath());

        // Find a resource if it exists
        Resource r = manager.getResourceFactory().getResource(host, url);
        if (r != null) {
            log.debug("locking existing resource: " + r.getName());
            processExistingResource(manager, request, response, r);
        } else {
            log.debug("lock target doesnt exist, attempting lock null..");
            processNonExistingResource(manager, request, response, host, url);
        }
    }

    protected void processExistingResource(HttpManager manager, Request request, Response response, Resource resource) throws NotAuthorizedException {
        if (handlerHelper.isNotCompatible(resource, request.getMethod()) || !isCompatible(resource)) {
            responseHandler.respondMethodNotImplemented(resource, response, request);
            return;
        }
        if (!handlerHelper.checkAuthorisation(manager, resource, request)) {
            responseHandler.respondUnauthorised(resource, response, request);
            return;
        }

        handlerHelper.checkExpects(responseHandler, request, response);

        LockableResource r = (LockableResource) resource;
        LockTimeout timeout = LockTimeout.parseTimeout(request);
        String ifHeader = request.getIfHeader();
        response.setContentTypeHeader(Response.XML);
        if (ifHeader == null || ifHeader.length() == 0) {
            processNewLock(manager, request, response, r, timeout);
        } else {
            processRefresh(manager, request, response, r, timeout, ifHeader);
        }
    }

    /**
     * (from the spec) 7.4 Write Locks and Null Resources
     *
     * It is possible to assert a write lock on a null resource in order to lock
     * the name.
     *
     * A write locked null resource, referred to as a lock-null resource, MUST
     * respond with a 404 (Not Found) or 405 (Method Not Allowed) to any
     * HTTP/1.1 or DAV methods except for PUT, MKCOL, OPTIONS, PROPFIND, LOCK,
     * and UNLOCK. A lock-null resource MUST appear as a member of its parent
     * collection. Additionally the lock-null resource MUST have defined on it
     * all mandatory DAV properties. Most of these properties, such as all the
     * get* properties, will have no value as a lock-null resource does not
     * support the GET method. Lock-Null resources MUST have defined values for
     * lockdiscovery and supportedlock properties.
     *
     * Until a method such as PUT or MKCOL is successfully executed on the
     * lock-null resource the resource MUST stay in the lock-null state.
     * However, once a PUT or MKCOL is successfully executed on a lock-null
     * resource the resource ceases to be in the lock-null state.
     *
     * If the resource is unlocked, for any reason, without a PUT, MKCOL, or
     * similar method having been successfully executed upon it then the
     * resource MUST return to the null state.
     *
     *
     * @param manager
     * @param request
     * @param response
     * @param host
     * @param url
     */
    private void processNonExistingResource(HttpManager manager, Request request, Response response, String host, String url) throws NotAuthorizedException, BadRequestException {
        String name;

        Path parentPath = Path.path(url);
        name = parentPath.getName();
        parentPath = parentPath.getParent();
        url = parentPath.toString();

        Resource r = manager.getResourceFactory().getResource(host, url);
        if (r != null) {
            if (!handlerHelper.checkAuthorisation(manager, r, request)) {
                responseHandler.respondUnauthorised(r, response, request);
                return;
            } else {
                processCreateAndLock(manager, request, response, r, name);
            }
        } else {
            log.debug("couldnt find parent to execute lock-null, returning not found");
            //respondNotFound(response,request);
            response.setStatus(Status.SC_CONFLICT);

        }
    }

    private void processCreateAndLock(HttpManager manager, Request request, Response response, Resource parentResource, String name) throws NotAuthorizedException {
        if (parentResource instanceof LockingCollectionResource) {
            log.debug("parent supports lock-null. doing createAndLock");
            LockingCollectionResource lockingParent = (LockingCollectionResource) parentResource;
            LockTimeout timeout = LockTimeout.parseTimeout(request);
            response.setContentTypeHeader(Response.XML);

            LockInfo lockInfo;
            try {
                lockInfo = LockInfoSaxHandler.parseLockInfo(request);
            } catch (SAXException ex) {
                throw new RuntimeException("Exception reading request body", ex);
            } catch (IOException ex) {
                throw new RuntimeException("Exception reading request body", ex);
            }

            // TODO: this should be refactored to return a LockResult as for existing entities

            log.debug("Creating lock on unmapped resource: " + name);
            LockToken tok = lockingParent.createAndLock(name, timeout, lockInfo);
            if (tok == null) {
                throw new RuntimeException("createAndLock returned null, from resource of type: " + lockingParent.getClass().getCanonicalName());
            }
            response.setStatus(Status.SC_CREATED);
            response.setLockTokenHeader("<opaquelocktoken:" + tok.tokenId + ">");  // spec says to set response header. See 8.10.1
            respondWithToken(tok, request, response);

        } else {
            log.debug("parent does not support lock-null, respondong method not allowed");
            responseHandler.respondMethodNotImplemented(parentResource, response, request);
        }
    }

    @Override
    public boolean isCompatible(Resource handler) {
        return handler instanceof LockableResource;
    }

    protected void processNewLock(HttpManager milton, Request request, Response response, LockableResource r, LockTimeout timeout) throws NotAuthorizedException {
        LockInfo lockInfo;
        try {
            lockInfo = LockInfoSaxHandler.parseLockInfo(request);
        } catch (SAXException ex) {
            throw new RuntimeException("Exception reading request body", ex);
        } catch (IOException ex) {
            throw new RuntimeException("Exception reading request body", ex);
        }

        if (handlerHelper.isLockedOut(request, r)) {
            this.responseHandler.respondLocked(request, response, r);
            return;
        }

        log.debug("locking: " + r.getName());
        LockResult result;
        try {
            result = r.lock(timeout, lockInfo);
        } catch (PreConditionFailedException ex) {
            responseHandler.respondPreconditionFailed(request, response, r);
            return;
        } catch (LockedException ex) {
            responseHandler.respondLocked(request, response, r);
            return;
        }

        if (result.isSuccessful()) {
            LockToken tok = result.getLockToken();
            log.debug("..locked ok: " + tok.tokenId);
            response.setLockTokenHeader("<opaquelocktoken:" + tok.tokenId + ">");  // spec says to set response header. See 8.10.1
            respondWithToken(tok, request, response);
        } else {
            respondWithLockFailure(result, request, response);
        }
    }

    protected void processRefresh(HttpManager milton, Request request, Response response, LockableResource r, LockTimeout timeout, String ifHeader) throws NotAuthorizedException {
        String token = parseToken(ifHeader);
        log.debug("refreshing lock: " + token);
        LockResult result;
        try {
            result = r.refreshLock(token);
        } catch (PreConditionFailedException ex) {
            responseHandler.respondPreconditionFailed(request, response, r);
            return;
        }
        if (result.isSuccessful()) {
            LockToken tok = result.getLockToken();
            respondWithToken(tok, request, response);
        } else {
            respondWithLockFailure(result, request, response);
        }
    }

    protected void respondWithToken(LockToken tok, Request request, Response response) {
        response.setStatus(Status.SC_OK);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlWriter writer = new XmlWriter(out);
        writer.writeXMLHeader();
        String d = WebDavProtocol.DAV_PREFIX;
        writer.open(d + ":prop  xmlns:" + d + "=\"DAV:\"");
        writer.newLine();
        writer.open(d + ":lockdiscovery");
        writer.newLine();
        writer.open(d + ":activelock");
        writer.newLine();
        lockWriterHelper.appendType(writer, tok.info.type);
        lockWriterHelper.appendScope(writer, tok.info.scope);
        lockWriterHelper.appendDepth(writer, tok.info.depth);
        lockWriterHelper.appendOwner(writer, tok.info.lockedByUser);
        lockWriterHelper.appendTimeout(writer, tok.timeout.getSeconds());
        lockWriterHelper.appendTokenId(writer, tok.tokenId);
        String url = PropFindPropertyBuilder.fixUrlForWindows(request.getAbsoluteUrl());
        lockWriterHelper.appendRoot(writer, url);
        writer.close(d + ":activelock");
        writer.close(d + ":lockdiscovery");
        writer.close(d + ":prop");
        writer.flush();

        LogUtils.debug(log, "lock response: ", out);
        response.setEntity(new ByteArrayEntity(out.toByteArray()));
//        response.close();

    }

    static String parseToken(String ifHeader) {
        String token = ifHeader;
        int pos = token.indexOf(":");
        if (pos >= 0) {
            token = token.substring(pos + 1);
            pos = token.indexOf(">");
            if (pos >= 0) {
                token = token.substring(0, pos);
            }
        }
        return token;
    }

    private void respondWithLockFailure(LockResult result, Request request, Response response) {
        log.info("respondWithLockFailure: " + result.getFailureReason().name());
        response.setStatus(result.getFailureReason().status);

    }

    /**
     * Display information about licensing. Implemented here because this is one
     * of the few classes in milton which is generally not replaceable.
     */
    private void displayCopyrightNotice() {
        Properties validatedLicenseProps = getValidatedLicenseProperties();
        System.out.println("Initializing Milton2 Webdav library. Checking for license file...");
        if (validatedLicenseProps == null) {
            System.out.println("No license file found. By using this software you are agreeing to the terms of the Affero GPL - http://www.gnu.org/licenses/agpl-3.0.html");
            System.out.println("For non-FOSS/commercial usage you should obtain a commercial license. Please see http://milton.io/license for details");
            System.out.println("Copyright McEvoy Software Limited");
            try {
                URL url = new URL("http://milton.io/downloads/version.txt");
                InputStream in = url.openStream();
                if( in != null ) {
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    IOUtils.copy(in, bout);
                    String latestVersion = bout.toString("UTF-8").trim();
                    in = LockHandler.class.getResourceAsStream("/META-INF/maven/io.milton/milton-server-ent/pom.properties");
                    //in = LockHandler.class.getResourceAsStream("/test/pom.properties");
                    if( in != null ) {
                        Properties props = new Properties();
                        props.load(in);
                        in.close();
                        String localVersion = props.getProperty("version");
                        if( localVersion != null ) {
                            localVersion = localVersion.trim();
                            if( !localVersion.equals(latestVersion)) {
                                System.out.println("A new version of Milton2 Webdav has been released: " + latestVersion + " - see http://milton.io/downloads");
                            } else {
                                System.out.println("using latest");
                            }
                        } else {
                            System.out.println("no version prop");
                        }
                    } else {
                        System.out.println("no meta information, can't check latest version");
                    }
                }
            } catch (Throwable e) {
                
            }

        } else {
            System.out.println("Milton2 license found:");
            for (String key : validatedLicenseProps.stringPropertyNames()) {
                System.out.println(key + ": " + validatedLicenseProps.getProperty(key));
            }
        }
    }

    public static Properties getValidatedLicenseProperties() {
        try {
            byte[] licenseBytes;
            {
                InputStream in = LockHandler.class.getResourceAsStream("/milton.license.properties");
                if (in == null) {
                    return null;
                }
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                IOUtils.copy(in, bout);
                licenseBytes = bout.toByteArray();
            }
            KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");

            Signature sig;
            {
                InputStream in = LockHandler.class.getResourceAsStream("/miltonPublicKey");
                if (in == null) {
                    System.out.println("No Milton2 public key file found on the classpath. Expected to find /miltonPublicKey - please contact the licensor at http://milton.io");
                    return null;
                }
                byte[] publicKey = new byte[in.available()];
                in.read(publicKey);
                in.close();
                publicKey = Base64.decodeBase64(publicKey);
                X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicKey);
                PublicKey pub = keyFactory.generatePublic(pubKeySpec);

                sig = Signature.getInstance("SHA1withDSA", "SUN");
                sig.initVerify(pub);
            }

            {
                ByteArrayInputStream bin = new ByteArrayInputStream(licenseBytes);
                byte[] buffer = new byte[1024];
                int len;
                while (bin.available() != 0) {
                    len = bin.read(buffer);
                    sig.update(buffer, 0, len);
                }
                bin.close();
            }

            InputStream in = LockHandler.class.getResourceAsStream("/milton.license.sig");
            if (in == null) {
                System.out.println("No Milton2 license signature found. Please create a classpath resource /milton.license.sig containg the signature provided, or contact the licensor at http://milton.io");
                return null;
            }
            byte[] sigToVerify = new byte[in.available()];
            in.read(sigToVerify);
            in.close();
            sigToVerify = Base64.decodeBase64(sigToVerify);

            boolean verifies = sig.verify(sigToVerify);

            if (verifies) {
                Properties props = new Properties();
                ByteArrayInputStream bin = new ByteArrayInputStream(licenseBytes);
                props.load(bin);

                if (props.containsKey("Expires")) {
                    String sExpires = props.getProperty("Expires");
                    if (sExpires != null && sExpires.trim().length() > 0) {
                        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date expiryDate = sdf.parse(sExpires);
                        Date now = new Date();
                        if (now.after(expiryDate)) {
                            System.out.println("WARNING: Your Milton2 license has expired. Please contact the licensor at http://milton.io");
                            return null;
                        }
                    }
                }
                return props;
            } else {
                System.out.println("Milton2 license signature is not valid for license file. Please check with the licensor at http://milton.io");
                return null;
            }

        } catch (Exception e) {
            System.err.println("Exception checking for milton commercial license: " + e.toString() + " If you have a commercial license please check with the licensor at http://milton.io");
            e.printStackTrace();
            return null;
        }
    }
}
