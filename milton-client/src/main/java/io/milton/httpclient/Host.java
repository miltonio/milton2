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
package io.milton.httpclient;

import io.milton.common.Path;
import io.milton.http.Range;
import io.milton.http.Response;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import com.ettrema.cache.Cache;
import com.ettrema.cache.MemoryCache;
import io.milton.httpclient.Utils.CancelledException;
import io.milton.httpclient.zsyncclient.FileSyncer;
import io.milton.common.LogUtils;
import io.milton.http.DateUtils;
import io.milton.http.DateUtils.DateParseException;
import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.namespace.QName;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mcevoyb
 */
public class Host extends Folder {

    public static List<QName> defaultFields = Arrays.asList(
            RespUtils.davName("resourcetype"),
            RespUtils.davName("etag"),
            RespUtils.davName("displayname"),
            RespUtils.davName("getcontentlength"),
            RespUtils.davName("creationdate"),
            RespUtils.davName("getlastmodified"),
            RespUtils.davName("iscollection"),
            RespUtils.davName("lockdiscovery"));
    private static String LOCK_XML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
            + "<D:lockinfo xmlns:D='DAV:'>"
            + "<D:lockscope><D:exclusive/></D:lockscope>"
            + "<D:locktype><D:write/></D:locktype>"
            + "<D:owner>${owner}</D:owner>"
            + "</D:lockinfo>";
    private static final Set<String> WEBDAV_REDIRECTABLE = new HashSet<String>(Arrays.asList(new String[]{"PROPFIND", "LOCK", "UNLOCK", "DELETE"}));
    private static final Logger log = LoggerFactory.getLogger(Host.class);
    public final String server;
    public final Integer port;
    public final String user;
    public final String password;
    public final String rootPath;
    /**
     * time in milliseconds to be used for all timeout parameters
     */
    private int timeout;
    private final DefaultHttpClient client;
    private final TransferService transferService;
    private final FileSyncer fileSyncer;
    private final List<ConnectionListener> connectionListeners = new ArrayList<ConnectionListener>();
    private boolean secure; // use HTTPS if true
    private boolean useDigestForPreemptiveAuth = true; // if true we will do pre-emptive auth with Digest, otherwise will use Basic

    static {
//    System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
//    System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
//    System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
//    System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");    
    }

    public Host(String server, Integer port, String user, String password, ProxyDetails proxyDetails) {
        this(server, null, port, user, password, proxyDetails, 30000, null, null);
    }

    public Host(String server, Integer port, String user, String password, ProxyDetails proxyDetails, Cache<Folder, List<Resource>> cache) {
        this(server, null, port, user, password, proxyDetails, 30000, cache, null); // defaul timeout of 30sec
    }

    public Host(String server, String rootPath, Integer port, String user, String password, ProxyDetails proxyDetails, Cache<Folder, List<Resource>> cache) {
        this(server, rootPath, port, user, password, proxyDetails, 30000, cache, null); // defaul timeout of 30sec
    }

    public Host(String server, String rootPath, Integer port, String user, String password, ProxyDetails proxyDetails, int timeoutMillis, Cache<Folder, List<Resource>> cache, FileSyncer fileSyncer) {
        super((cache != null ? cache : new MemoryCache<Folder, List<Resource>>("resource-cache-default", 50, 20)));
        if (server == null) {
            throw new IllegalArgumentException("host name cannot be null");
        }
        if (rootPath != null) {
            String rp = rootPath;
            if (rp.startsWith("/")) { // strip leading slash so can be concatenated
                rp = rp.substring(1);
            }
            this.rootPath = rp;
        } else {
            this.rootPath = null;
        }
        this.timeout = timeoutMillis;
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
        HttpConnectionParams.setSoTimeout(params, 10000);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

        // Create and initialize scheme registry 
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

        // Create an HttpClient with the ThreadSafeClientConnManager.
        // This connection manager must be used if more than one thread will
        // be using the HttpClient.
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(schemeRegistry);
        cm.setMaxTotal(200);

        client = new MyDefaultHttpClient(cm, params);
        HttpRequestRetryHandler handler = new NoRetryHttpRequestRetryHandler();
        client.setHttpRequestRetryHandler(handler);
        client.setRedirectStrategy(new DefaultRedirectStrategy() {
            @Override
            public boolean isRedirected(
                    final HttpRequest request,
                    final HttpResponse response,
                    final HttpContext context) throws ProtocolException {

                if (super.isRedirected(request, response, context)) {
                    return true;
                }
                int statusCode = response.getStatusLine().getStatusCode();
                String method = request.getRequestLine().getMethod();
                Header locationHeader = response.getFirstHeader("location");
                switch (statusCode) {
                    case HttpStatus.SC_MOVED_TEMPORARILY:
                        return locationHeader != null && WEBDAV_REDIRECTABLE.contains(method);
                    case HttpStatus.SC_MOVED_PERMANENTLY:
                    case HttpStatus.SC_TEMPORARY_REDIRECT:
                        return WEBDAV_REDIRECTABLE.contains(method);
                    default:
                        return false;
                }
            }
        });        

        if (user != null) {
            client.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
            PreemptiveAuthInterceptor interceptor = new PreemptiveAuthInterceptor();
            client.addRequestInterceptor(interceptor, 0);
        }


        if (proxyDetails != null) {
            if (proxyDetails.isUseSystemProxy()) {
                System.setProperty("java.net.useSystemProxies", "true");
            } else {
                System.setProperty("java.net.useSystemProxies", "false");
                if (proxyDetails.getProxyHost() != null && proxyDetails.getProxyHost().length() > 0) {
                    HttpHost proxy = new HttpHost(proxyDetails.getProxyHost(), proxyDetails.getProxyPort(), "http");
                    client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                    if (proxyDetails.hasAuth()) {
                        client.getCredentialsProvider().setCredentials(
                                new AuthScope(proxyDetails.getProxyHost(), proxyDetails.getProxyPort()),
                                new UsernamePasswordCredentials(proxyDetails.getUserName(), proxyDetails.getPassword()));
                    }
                }
            }
        }
        transferService = new TransferService(client, connectionListeners);
        transferService.setTimeout(timeoutMillis);
        this.fileSyncer = fileSyncer;
    }

    /**
     * Finds the resource by iterating through the path parts resolving
     * collections as it goes. If any path component is not founfd returns null
     *
     * @param path
     * @return
     * @throws IOException
     * @throws com.ettrema.httpclient.HttpException
     */
    public Resource find(String path) throws IOException, io.milton.httpclient.HttpException, NotAuthorizedException, BadRequestException {
        return find(path, false);
    }

    public Resource find(String path, boolean invalidateCache) throws IOException, io.milton.httpclient.HttpException, NotAuthorizedException, BadRequestException {
        if (path == null || path.length() == 0 || path.equals("/")) {
            return this;
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        String[] arr = path.split("/");
        return _find(this, arr, 0, invalidateCache);
    }

    public static Resource _find(Folder parent, String[] arr, int i, boolean invalidateCache) throws IOException, io.milton.httpclient.HttpException, NotAuthorizedException, BadRequestException {
        String childName = arr[i];
        if (invalidateCache) {
            parent.flush();
        }
        Resource child = parent.child(childName);
        if (i == arr.length - 1) {
            return child;
        } else {
            if (child instanceof Folder) {
                return _find((Folder) child, arr, i + 1, invalidateCache);
            } else {
                return null;
            }
        }
    }

    /**
     * Find a folder at the given path. Is much the same as find(path), except
     * that it throws an exception if the resource is not a folder
     *
     * @param path
     * @return
     * @throws IOException
     * @throws com.ettrema.httpclient.HttpException
     * @throws NotAuthorizedException
     * @throws BadRequestException
     */
    public Folder getFolder(String path) throws IOException, io.milton.httpclient.HttpException, NotAuthorizedException, BadRequestException {
        Resource res = find(path);
        if (res instanceof Folder) {
            return (Folder) res;
        } else {
            throw new RuntimeException("Not a folder: " + res.href());
        }
    }

    /**
     * Create a collection at the given absolute path. This path is NOT relative
     * to the host's base path
     *
     * @param newUri
     * @return
     * @throws com.ettrema.httpclient.HttpException
     * @throws NotAuthorizedException
     * @throws ConflictException
     * @throws BadRequestException
     * @throws NotFoundException
     * @throws URISyntaxException
     */
    public synchronized int doMkCol(Path newUri) throws io.milton.httpclient.HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException, URISyntaxException {
        String url = this.buildEncodedUrl(newUri);
        return doMkCol(url);
    }

    /**
     *
     * @param newUri - must be fully qualified and correctly encoded
     * @return
     * @throws com.ettrema.httpclient.HttpException
     */
    public synchronized int doMkCol(String newUri) throws io.milton.httpclient.HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException, URISyntaxException {
        notifyStartRequest();
        MkColMethod p = new MkColMethod(newUri);
        try {
            int result = Utils.executeHttpWithStatus(client, p, null, newContext());
            if (result == 409) {
                // probably means the folder already exists
                p.abort();
                return result;
            }
            Utils.processResultCode(result, newUri);
            return result;
        } catch (IOException ex) {
            p.abort();
            throw new RuntimeException(ex);
        } finally {
            notifyFinishRequest();
        }
    }

    /**
     * Attempts to lock a resource with infinite timeout and returns the lock
     * token, which must be retained to unlock the resource
     *
     * @param uri - must be encoded
     * @return
     */
    public synchronized String doLock(String uri) throws io.milton.httpclient.HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException, URISyntaxException {
        return doLock(uri, -1);
    }

    /**
     * Attempts to lock a resource with the specified timeout and returns the
     * lock token, which must be retained to unlock the resource
     *
     * @param uri - must be encoded
     * @param timeout lock timeout in seconds, or -1 if infinite
     * @return
     * @throws com.ettrema.httpclient.HttpException
     */
    public synchronized String doLock(String uri, int timeout) throws io.milton.httpclient.HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException, URISyntaxException {
        notifyStartRequest();
        LockMethod p = new LockMethod(uri, timeout);
        try {
            String lockXml = LOCK_XML.replace("${owner}", user);
            HttpEntity requestEntity = new StringEntity(lockXml, "UTF-8");
            p.setEntity(requestEntity);
            HttpResponse resp = host().client.execute(p);
            int result = resp.getStatusLine().getStatusCode();
            Utils.processResultCode(result, uri);
            return p.getLockToken(resp);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            notifyFinishRequest();
        }
    }

    /**
     *
     * @param uri - must be encoded
     * @param lockToken
     * @return
     * @throws com.ettrema.httpclient.HttpException
     */
    public synchronized int doUnLock(String uri, String lockToken) throws io.milton.httpclient.HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException, URISyntaxException {
        notifyStartRequest();
        UnLockMethod p = new UnLockMethod(uri, lockToken);
        try {
            int result = Utils.executeHttpWithStatus(client, p, null, newContext());
            Utils.processResultCode(result, uri);
            return result;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            notifyFinishRequest();
        }
    }

    /**
     *
     * @param path - an Un-encoded path. Eg /a/b c/ = /a/b%20c/
     * @param content
     * @param contentLength
     * @param contentType
     * @return
     */
    public HttpResult doPut(Path path, InputStream content, Long contentLength, String contentType, IfMatchCheck matchCheck) {
        String dest = buildEncodedUrl(path);
        return doPut(dest, content, contentLength, contentType, matchCheck, null);
    }

    public HttpResult doPut(Path path, byte[] data, String contentType) {
        String dest = buildEncodedUrl(path);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        return transferService.put(dest, bin, (long) data.length, contentType, null, null, newContext());
    }

    /**
     *
     * @param newUri
     * @param file
     * @param listener
     * @return - the result code
     * @throws FileNotFoundException
     * @throws HttpException
     */
    public HttpResult doPut(Path remotePath, java.io.File file, IfMatchCheck matchCheck, ProgressListener listener) throws FileNotFoundException, HttpException, CancelledException, NotAuthorizedException, ConflictException {
        if (fileSyncer != null) {
            try {
                fileSyncer.upload(this, file, remotePath, listener);
                LogUtils.trace(log, "doPut: uploaded");
                return new HttpResult(Response.Status.SC_OK.code, null);
            } catch (NotFoundException e) {
                // ZSync file was not found
                log.trace("Not found: " + remotePath);
            } catch (IOException ex) {
                throw new GenericHttpException(remotePath.toString(), ex);
            }
        }
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            String dest = buildEncodedUrl(remotePath);
            return doPut(dest, in, file.length(), null, matchCheck, listener);
        } finally {
            IOUtils.closeQuietly(in);
        }

    }

    /**
     * Uploads the data. Does not do any file syncronisation
     *
     * @param newUri - encoded full URL
     * @param content
     * @param contentLength
     * @param contentType
     * @param etag - expected etag on the server, or null if a new file
     * @return - the result code
     */
    public synchronized HttpResult doPut(String newUri, InputStream content, Long contentLength, String contentType, IfMatchCheck matchCheck, ProgressListener listener) {
        LogUtils.trace(log, "doPut", newUri);
        return transferService.put(newUri, content, contentLength, contentType, matchCheck, listener, newContext());
    }

    /**
     *
     * @param from - encoded source url
     * @param newUri - encoded destination
     * @return
     * @throws com.ettrema.httpclient.HttpException
     */
    public synchronized int doCopy(String from, String newUri) throws io.milton.httpclient.HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException, URISyntaxException {
        notifyStartRequest();
        CopyMethod m = new CopyMethod(from, newUri);
        m.addHeader("Overwrite", "T");
        try {
            int res = Utils.executeHttpWithStatus(client, m, null, newContext());
            Utils.processResultCode(res, from);
            return res;
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            notifyFinishRequest();
        }

    }

    /**
     * Deletes the item at the given path, relative to the root path of this
     * host
     *
     * @param path - unencoded and relative to Host's rootPath
     * @return
     * @throws IOException
     * @throws com.ettrema.httpclient.HttpException
     * @throws NotAuthorizedException
     * @throws ConflictException
     * @throws BadRequestException
     * @throws NotFoundException
     */
    public synchronized int doDelete(Path path) throws IOException, io.milton.httpclient.HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        String dest = buildEncodedUrl(path);
        return doDelete(dest);
    }

    /**
     *
     * @param url - encoded url
     * @return
     * @throws IOException
     * @throws com.ettrema.httpclient.HttpException
     */
    public synchronized int doDelete(String url) throws IOException, io.milton.httpclient.HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        notifyStartRequest();
        HttpDelete m = new HttpDelete(url);
        try {
            int res = Utils.executeHttpWithStatus(client, m, null, newContext());
            Utils.processResultCode(res, url);
            return res;
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        } finally {
            notifyFinishRequest();
        }
    }

    /**
     *
     * @param sourceUrl - encoded source url
     * @param newUri - encoded destination url
     * @return
     * @throws IOException
     * @throws com.ettrema.httpclient.HttpException
     */
    public synchronized int doMove(String sourceUrl, String newUri) throws IOException, io.milton.httpclient.HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException, URISyntaxException {
        notifyStartRequest();
        MoveMethod m = new MoveMethod(sourceUrl, newUri);
        try {
            int res = Utils.executeHttpWithStatus(client, m, null, newContext());
            Utils.processResultCode(res, sourceUrl);
            return res;
        } finally {
            notifyFinishRequest();
        }
    }

    public synchronized List<PropFindResponse> propFind(Path path, int depth, QName... fields) throws IOException, io.milton.httpclient.HttpException, NotAuthorizedException, BadRequestException {
        List<QName> list = new ArrayList<QName>();
        list.addAll(Arrays.asList(fields));
        return propFind(path, depth, list);
    }

    /**
     *
     * @param path - unencoded path, which will be evaluated relative to this
     * Host's basePath
     * @param depth - 1 is to find immediate children, 2 includes their
     * children, etc
     * @param fields - the list of fields to get, or null to use default fields
     * @return
     * @throws IOException
     * @throws com.ettrema.httpclient.HttpException
     * @throws NotAuthorizedException
     * @throws BadRequestException
     */
    public synchronized List<PropFindResponse> propFind(Path path, int depth, List<QName> fields) throws IOException, io.milton.httpclient.HttpException, NotAuthorizedException, BadRequestException {
        String url = buildEncodedUrl(path);
        return _doPropFind(url, depth, fields);
    }

    /**
     *
     * @param url - the encoded absolute URL to query. This method does not
     * apply basePath
     * @param depth - depth to generate responses for. Zero means only the
     * specified url, 1 means it and its direct children, etc
     * @return
     * @throws IOException
     * @throws com.ettrema.httpclient.HttpException
     */
    public synchronized List<PropFindResponse> _doPropFind(final String url, final int depth, List<QName> fields) throws IOException, io.milton.httpclient.HttpException, NotAuthorizedException, BadRequestException {
        log.trace("doPropFind: " + url);
        notifyStartRequest();
        final PropFindMethod m = new PropFindMethod(url);
        m.addHeader("Depth", depth + "");
        m.addHeader("Accept-Charset", "utf-8,*;q=0.1");
        m.addHeader("Accept", "text/xml");

        try {
            String propFindXml = buildPropFindXml(fields);
            HttpEntity requestEntity = new StringEntity(propFindXml, "text/xml", "UTF-8");
            m.setEntity(requestEntity);

            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            final List<PropFindResponse> responses = new ArrayList<PropFindResponse>();
            ResponseHandler<Integer> respHandler = new ResponseHandler<Integer>() {
                @Override
                public Integer handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    Header serverDateHeader = response.getFirstHeader("Date");
                    if (response.getStatusLine().getStatusCode() == 207) {
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            entity.writeTo(bout);
                            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
                            Document document = getResponseAsDocument(bin);
                            String sServerDate = null;
                            if (serverDateHeader != null) {
                                sServerDate = serverDateHeader.getValue();
                            }
                            Date serverDate = null;
                            if (sServerDate != null && sServerDate.length() > 0) {
                                try {
                                    serverDate = DateUtils.parseDate(sServerDate);
                                } catch (DateParseException ex) {
                                    log.warn("Couldnt parse date header: " + sServerDate, ex);
                                }
                            }
                            //System.out.println("propfind: " + url);
                            buildResponses(document, serverDate, responses, depth);

                        }
                    }
                    return response.getStatusLine().getStatusCode();
                }
            };
            Integer res = client.execute(m, respHandler, newContext());

            Utils.processResultCode(res, url);
            return responses;
        } catch (ConflictException ex) {
            throw new RuntimeException(ex);
        } catch (NotFoundException e) {
            log.trace("not found: " + url);
            return null;
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        } finally {
            notifyFinishRequest();
        }
    }

    /**
     *
     * @return - child responses only, not the requested url
     */
    public void buildResponses(Document document, Date serverDate, List<PropFindResponse> responses, int depth) {
        Element root = document.getRootElement();
        List<Element> responseEls = RespUtils.getElements(root, "response");
        boolean isFirst = true;
        for (Element el : responseEls) {
            if (!isFirst || depth == 0) { // if depth=0 must return first and only result
                PropFindResponse resp = new PropFindResponse(serverDate, el);
                //String href = resp.getHref();
                responses.add(resp);
            } else {
                isFirst = false;
            }
        }
    }

    public Document getResponseAsDocument(InputStream in) throws IOException {
//        IOUtils.copy( in, out );
//        String xml = out.toString();
        try {
            Document document = RespUtils.getJDomDocument(in);
            return document;
        } catch (JDOMException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     *
     * @param url - fully qualified and encoded URL
     * @param receiver
     * @param rangeList - if null does a normal GET request
     * @throws com.ettrema.httpclient.HttpException
     * @throws com.ettrema.httpclient.Utils.CancelledException
     */
    public synchronized void doGet(String url, StreamReceiver receiver, List<Range> rangeList, ProgressListener listener) throws io.milton.httpclient.HttpException, Utils.CancelledException, NotAuthorizedException, BadRequestException, ConflictException, NotFoundException {
        transferService.get(url, receiver, rangeList, listener, newContext());
    }

    /**
     *
     * @param path - the path to get, relative to the base path of the host
     * @param file - the file to write content to
     * @param listener
     * @throws IOException
     * @throws NotFoundException
     * @throws com.ettrema.httpclient.HttpException
     * @throws com.ettrema.httpclient.Utils.CancelledException
     * @throws NotAuthorizedException
     * @throws BadRequestException
     * @throws ConflictException
     */
    public synchronized void doGet(Path path, final java.io.File file, ProgressListener listener) throws IOException, NotFoundException, io.milton.httpclient.HttpException, CancelledException, NotAuthorizedException, BadRequestException, ConflictException {
        LogUtils.trace(log, "doGet", path);
        if (fileSyncer != null) {
            fileSyncer.download(this, path, file, listener);
        } else {
            String url = this.buildEncodedUrl(path);
            transferService.get(url, new StreamReceiver() {
                @Override
                public void receive(InputStream in) throws IOException {
                    OutputStream out = null;
                    BufferedOutputStream bout = null;
                    try {
                        out = FileUtils.openOutputStream(file);
                        bout = new BufferedOutputStream(out);
                        IOUtils.copy(in, bout);
                        bout.flush();
                    } finally {
                        IOUtils.closeQuietly(bout);
                        IOUtils.closeQuietly(out);
                    }

                }
            }, null, listener, newContext());
        }
    }

    public synchronized byte[] doGet(Path path) throws IOException, NotFoundException, io.milton.httpclient.HttpException, CancelledException, NotAuthorizedException, BadRequestException, ConflictException {
        return doGet(path, null);
    }

    public synchronized byte[] doGet(Path path, Map<String, String> queryParams) throws IOException, NotFoundException, io.milton.httpclient.HttpException, CancelledException, NotAuthorizedException, BadRequestException, ConflictException {
        LogUtils.trace(log, "doGet", path);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        doGet(path, bout, queryParams);
        return bout.toByteArray();

    }

    public synchronized void doGet(Path path, final OutputStream out, Map<String, String> queryParams) throws IOException, NotFoundException, io.milton.httpclient.HttpException, CancelledException, NotAuthorizedException, BadRequestException, ConflictException {
        String url = this.buildEncodedUrl(path);
        LogUtils.trace(log, "doGet", url);
        if (queryParams != null && queryParams.size() > 0) {
            String qs = Utils.format(queryParams, "UTF-8");
            url += "?" + qs;
        }
        transferService.get(url, new StreamReceiver() {
            @Override
            public void receive(InputStream in) throws IOException {
                IOUtils.copy(in, out);
            }
        }, null, null, newContext());
    }

    /**
     *
     * @param path - encoded path, but not fully qualified. Must not be prefixed
     * with a slash, as it will be appended to the host's URL
     * @throws java.net.ConnectException
     * @throws Unauthorized
     * @throws UnknownHostException
     * @throws SocketTimeoutException
     * @throws IOException
     * @throws com.ettrema.httpclient.HttpException
     */
    public synchronized void options(String path) throws java.net.ConnectException, NotAuthorizedException, UnknownHostException, SocketTimeoutException, IOException, io.milton.httpclient.HttpException, NotFoundException {
        String url = this.encodedUrl() + path;
        doOptions(url);
    }

    public void doOptions(Path path) throws NotFoundException, java.net.ConnectException, NotAuthorizedException, java.net.UnknownHostException, SocketTimeoutException, IOException, io.milton.httpclient.HttpException {
        String dest = buildEncodedUrl(path);
        doOptions(dest);
    }

    private synchronized void doOptions(String url) throws NotFoundException, java.net.ConnectException, NotAuthorizedException, java.net.UnknownHostException, SocketTimeoutException, IOException, io.milton.httpclient.HttpException {
        notifyStartRequest();
        String uri = url;
        log.trace("doOptions: {}", url);
        HttpOptions m = new HttpOptions(uri);
        InputStream in = null;
        try {
            int res = Utils.executeHttpWithStatus(client, m, null, newContext());
            log.trace("result code: " + res);
            if (res == 301 || res == 302) {
                return;
            }
            Utils.processResultCode(res, url);
        } catch (ConflictException ex) {
            throw new RuntimeException(ex);
        } catch (BadRequestException ex) {
            throw new RuntimeException(ex);
        } finally {
            Utils.close(in);
            notifyFinishRequest();
        }
    }

    /**
     * GET the contents of the given path. The path is non-encoded, and it
     * relative to the host's root.
     *
     * @param path
     * @return
     * @throws com.ettrema.httpclient.HttpException
     * @throws NotAuthorizedException
     * @throws BadRequestException
     * @throws ConflictException
     * @throws NotFoundException
     */
    public synchronized byte[] get(Path path) throws io.milton.httpclient.HttpException, NotAuthorizedException, BadRequestException, ConflictException, NotFoundException {
        String url = buildEncodedUrl(path);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            transferService.get(url, new StreamReceiver() {
                @Override
                public void receive(InputStream in) {
                    try {
                        IOUtils.copy(in, out);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }, null, null, newContext());
        } catch (CancelledException ex) {
            throw new RuntimeException("Should never happen because no progress listener is set", ex);
        }
        return out.toByteArray();
    }

    /**
     * Retrieve the bytes at the specified path.
     *
     * @param path - encoded and relative to host's rootPath. Must NOT be slash
     * prefixed as it will be appended to the host's url
     * @return
     * @throws com.ettrema.httpclient.HttpException
     */
    public synchronized byte[] get(String path) throws io.milton.httpclient.HttpException, NotAuthorizedException, BadRequestException, ConflictException, NotFoundException {
        String url = this.encodedUrl() + path;
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            transferService.get(url, new StreamReceiver() {
                @Override
                public void receive(InputStream in) {
                    try {
                        IOUtils.copy(in, out);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }, null, null, newContext());
        } catch (CancelledException ex) {
            throw new RuntimeException("Should never happen because no progress listener is set", ex);
        }
        return out.toByteArray();
    }

    /**
     * POSTs the variables and returns the body
     *
     * @param url - fully qualified and encoded URL to post to
     * @param params
     * @return - the body of the response
     */
    public String doPost(String url, Map<String, String> params) throws io.milton.httpclient.HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        notifyStartRequest();
        HttpPost m = new HttpPost(url);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        for (Entry<String, String> entry : params.entrySet()) {
            formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        UrlEncodedFormEntity entity;
        try {
            entity = new UrlEncodedFormEntity(formparams);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
        m.setEntity(entity);
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            int res = Utils.executeHttpWithStatus(client, m, bout, newContext());
            Utils.processResultCode(res, url);
            return bout.toString();
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            notifyFinishRequest();
        }
    }

    /**
     *
     * @param url - fully qualified and encoded
     * @param params
     * @param parts
     * @return
     * @throws com.ettrema.httpclient.HttpException
     */
//    public String doPost(String url, Map<String, String> params, Part[] parts) throws com.ettrema.httpclient.HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
//        notifyStartRequest();
//        PostMethod filePost = new PostMethod(url);
//        if (params != null) {
//            for (Entry<String, String> entry : params.entrySet()) {
//                filePost.addParameter(entry.getKey(), entry.getValue());
//            }
//        }
//        filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
//
//        InputStream in = null;
//        try {
//            int res = client.executeMethod(filePost);
//            Utils.processResultCode(res, url);
//            in = filePost.getResponseBodyAsStream();
//            ByteArrayOutputStream bout = new ByteArrayOutputStream();
//            IOUtils.copy(in, bout);
//            return bout.toString();
//        } catch (HttpException ex) {
//            throw new RuntimeException(ex);
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        } finally {
//            Utils.close(in);
//            filePost.releaseConnection();
//            notifyFinishRequest();
//        }
//    }
    @Override
    public Host host() {
        return this;
    }

    @Override
    public String href() {
        String s = baseHref();
        if (rootPath != null) {
            s += rootPath;
        }
        if (!s.endsWith("/")) {
            s += "/";
        }
        return s;
    }

    public String baseHref() {
        String s = "http";
        int defaultPort = 80;
        if (secure) {
            s += "s";
            defaultPort = 443;
        }
        s += "://" + server;
        if (this.port != null && this.port != defaultPort && this.port > 0) {
            s += ":" + this.port;
        }

        s += "/";
        return s;
    }

    /**
     * Returns the fully qualified URL for the given path
     *
     * @param path
     * @return
     */
    public String getHref(Path path) {
        String s = href();

        if (!path.isRelative()) {
            s = s.substring(0, s.length() - 1);
        }
        //log.trace("host href: " + s);
        return s + path; // path will be absolute
    }

    @Override
    public String encodedUrl() {
        String s = buildEncodedUrl(Path.root);
        if (!s.endsWith("/")) {
            s += "/";
        }
        return s;
    }

    public io.milton.httpclient.Folder getOrCreateFolder(Path remoteParentPath, boolean create) throws io.milton.httpclient.HttpException, IOException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        log.trace("getOrCreateFolder: {}", remoteParentPath);
        io.milton.httpclient.Folder f = this;
        if (remoteParentPath != null) {
            for (String childName : remoteParentPath.getParts()) {
                if (childName.equals("_code")) {
                    f = new Folder(f, childName, cache);
                } else {
                    io.milton.httpclient.Resource child = f.child(childName);
                    if (child == null) {
                        if (create) {
                            f = f.createFolder(childName);
                        } else {
                            return null;
                        }
                    } else if (child instanceof io.milton.httpclient.Folder) {
                        f = (io.milton.httpclient.Folder) child;
                    } else {
                        log.warn("Can't upload. A resource exists with the same name as a folder, but is a file: " + remoteParentPath + " - " + child.getClass());
                        return null;
                    }
                }

            }
        }
        return f;
    }

    /**
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
        transferService.setTimeout(timeout);
    }

    private void notifyStartRequest() {
        for (ConnectionListener l : connectionListeners) {
            l.onStartRequest();
        }
    }

    private void notifyFinishRequest() {
        for (ConnectionListener l : connectionListeners) {
            l.onFinishRequest();
        }
    }

    public void addConnectionListener(ConnectionListener e) {
        connectionListeners.add(e);
    }

    public String buildEncodedUrl(Path path) {
        Path base = Path.path(rootPath);
        Path p = base.add(path);
        return baseHref() + Utils.buildEncodedUrl(p);
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public HttpClient getClient() {
        return client;
    }

    /**
     * TODO: should optimise so it only generates once per set of fields
     *
     * @param fields
     * @return
     */
    private String buildPropFindXml(List<QName> fields) {
        try {
            if (fields == null) {
                fields = defaultFields;
            }
            Element elPropfind = new Element("propfind", RespUtils.NS_DAV);
            Document doc = new Document(elPropfind);
            Element elProp = new Element("prop", RespUtils.NS_DAV);
            elPropfind.addContent(elProp);
            for (QName qn : fields) {
                Element elName = new Element(qn.getLocalPart(), qn.getPrefix(), qn.getNamespaceURI());
                elProp.addContent(elName);
            }
            XMLOutputter outputter = new XMLOutputter();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            outputter.output(doc, out);
            return out.toString("UTF-8");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean isUseDigestForPreemptiveAuth() {
        return useDigestForPreemptiveAuth;
    }

    public void setUseDigestForPreemptiveAuth(boolean useDigestForPreemptiveAuth) {
        this.useDigestForPreemptiveAuth = useDigestForPreemptiveAuth;
    }

    protected HttpContext newContext() {
        HttpContext context = new BasicHttpContext();
        AuthScheme authScheme;
        if (useDigestForPreemptiveAuth) {
            authScheme = new DigestScheme();
        } else {
            authScheme = new BasicScheme();
        }
        context.setAttribute("preemptive-auth", authScheme);
        return context;
    }

    static class PreemptiveAuthInterceptor implements HttpRequestInterceptor {

        private String nonce;
        private String realm;

        public PreemptiveAuthInterceptor() {
        }

        @Override
        public void process(final HttpRequest request, final HttpContext context) {
            AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);

            // If no auth scheme avaialble yet, try to initialize it
            // preemptively
            if (authState.getAuthScheme() == null) {
                AuthScheme authScheme = (AuthScheme) context.getAttribute("preemptive-auth");
                //AuthScheme authScheme = cachedAuthScheme;
                if (authScheme != null) {
                    boolean canDoAuth = false;
                    if (authScheme instanceof DigestScheme) {
                        DigestScheme d = (DigestScheme) authScheme;
                        if (nonce != null) {
                            d.overrideParamter("nonce", nonce);
                        }
                        if (realm != null) {
                            d.overrideParamter("realm", realm);
                            canDoAuth = true;
                        }
                    } else if (authScheme instanceof BasicScheme) {
                        canDoAuth = true;
                    }
                    if (canDoAuth) {
                        CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
                        HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                        Credentials creds = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()));
                        if (creds == null) {
                            throw new RuntimeException("No credentials for preemptive authentication");
                        }
                        authState.setAuthScheme(authScheme);
                        authState.setCredentials(creds);
                    }
                }
            } else {
                if (authState.getAuthScheme() instanceof DigestScheme) {
                    DigestScheme scheme = (DigestScheme) authState.getAuthScheme();
                    nonce = scheme.getParameter("nonce");
                    realm = scheme.getParameter("realm");
//                    log.info("PreemptiveAuthInterceptor: record cached realm: " + realm + " and nonce: " + nonce);
                }

            }

        }
    }

    static class NoRetryHttpRequestRetryHandler implements HttpRequestRetryHandler {

        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            return false;
        }
    }

    static class MyDefaultHttpClient extends DefaultHttpClient {

        public MyDefaultHttpClient(ClientConnectionManager cm, HttpParams params) {
            super(cm, params);
        }

        @Override
        protected HttpRequestRetryHandler createHttpRequestRetryHandler() {
            return new NoRetryHttpRequestRetryHandler();
        }

        @Override
        protected RequestDirector createClientRequestDirector(HttpRequestExecutor requestExec, ClientConnectionManager conman, ConnectionReuseStrategy reustrat, ConnectionKeepAliveStrategy kastrat, HttpRoutePlanner rouplan, HttpProcessor httpProcessor, HttpRequestRetryHandler retryHandler, RedirectStrategy redirectStrategy, AuthenticationHandler targetAuthHandler, AuthenticationHandler proxyAuthHandler, UserTokenHandler stateHandler, HttpParams params) {
            RequestDirector rd = super.createClientRequestDirector(requestExec, conman, reustrat, kastrat, rouplan, httpProcessor, retryHandler, redirectStrategy, targetAuthHandler, proxyAuthHandler, stateHandler, params);
            return rd;
        }
    }
}
