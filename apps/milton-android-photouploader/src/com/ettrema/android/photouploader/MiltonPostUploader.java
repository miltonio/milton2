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

package com.ettrema.android.photouploader;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

/**
 *
 * @author brad
 */
public class MiltonPostUploader implements Runnable {

    private static final String TAG = "MiltonUploader";

    private final Context context;
    private final ExecutorService queue;
    private final ImageItem item;
    private final Config config;
    private final int retries;

    /**
     * Constructor
     *
     * @param context Application context
     * @param queue Queue that handles image uploads
     * @param item Image queue item
     * @param retries Number of retries for failed uploads
     */
    public MiltonPostUploader( Context context, ExecutorService queue, ImageItem item, Config config, int retries ) {
        this.context = context;
        this.queue = queue;
        this.item = item;
        this.config = config;
        this.retries = retries;
    }

    /**
     * Upload image to Picasa
     */
    public void run() {
        Log.i(TAG, "run");
        // create items for http client
        UploadNotification notification = new UploadNotification( context, item.imageId, item.imageSize, item.imageName );
        String url = config.getBaseUrl();
        if( !url.endsWith( "/" ) ) {
            url += "/";
        }
        url += "_DAV/PUT";
        Log.i(TAG, "url: " + url);
        DefaultHttpClient client = new DefaultHttpClient();
        String user = config.getUserName();
        String password = config.getPassword();

        if( user != null && user.length() > 0 ) {
            client.getCredentialsProvider().setCredentials( AuthScope.ANY, new UsernamePasswordCredentials( user, password ) );

            // All this bollocks is just for pre-emptive authentication. It used to be a boolean...
            BasicHttpContext localcontext = new BasicHttpContext();
            BasicScheme basicAuth = new BasicScheme();
            localcontext.setAttribute( "preemptive-auth", basicAuth );
            client.addRequestInterceptor( new PreemptiveAuth(), 0 );
        }

        HttpPost post = new HttpPost( url );

        try {
            // new file and and entity
            File file = new File( item.imagePath );
            String id = "---------------------------28617237579832";
            Multipart multipart = new Multipart( item.imageName, id );

            multipart.addPart( file, item.imageType );

            // create new Multipart entity
            MultipartNotificationEntity entity = new MultipartNotificationEntity( multipart, notification );

            // get http params
            HttpParams params = client.getParams();

            // set protocal and timeout for httpclient
            params.setParameter( CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1 );
            params.setParameter( CoreConnectionPNames.SO_TIMEOUT, new Integer( 15000 ) );
            params.setParameter( CoreConnectionPNames.CONNECTION_TIMEOUT, new Integer( 15000 ) );

            // set body with upload entity
            post.setEntity( entity );

            post.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);

            post.addHeader( "Content-Type", "multipart/form-data; boundary=" + id );



            // execute upload to picasa and get response and status
            Log.i(TAG, "execute");
            HttpResponse response = client.execute( post );
            StatusLine line = response.getStatusLine();

            // return code indicates upload failed so throw exception
            if( line.getStatusCode() > 201 ) {
                throw new Exception( "Failed upload" );
            }

            // shut down connection
            client.getConnectionManager().shutdown();

            // notify user that file has been uploaded
            notification.finished();
        } catch( Exception e ) {
            Log.e(TAG, "exception: " + url, e);
            // file upload failed so abort post and close connection
            post.abort();
            client.getConnectionManager().shutdown();

            // get user preferences and number of retries for failed upload
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( context );
            int maxRetries = Integer.valueOf( prefs.getString( "retries", "" ).substring( 1 ) );

            // check if we can connect to internet and if we still have any tries left
            // to try upload again
            if( CheckInternet.getInstance().canConnect( context, prefs ) && retries < maxRetries ) {
                // remove notification for failed upload and queue item again
                Log.i(TAG, "will retry");
                notification.remove();
                queue.execute( new MiltonPutUploader( context, queue, item, config, retries+1 ) );
            } else {
                // upload failed, so let's notify user
                Log.i(TAG, "will not retry");
                notification.failed();
            }
        }
    }

    static class PreemptiveAuth implements HttpRequestInterceptor {

        public void process(
            final HttpRequest request,
            final HttpContext context ) throws HttpException, IOException {

            AuthState authState = (AuthState) context.getAttribute(
                ClientContext.TARGET_AUTH_STATE );

            // If no auth scheme avaialble yet, try to initialize it preemptively
            if( authState.getAuthScheme() == null ) {
                AuthScheme authScheme = (AuthScheme) context.getAttribute(
                    "preemptive-auth" );
                CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(
                    ClientContext.CREDS_PROVIDER );
                HttpHost targetHost = (HttpHost) context.getAttribute(
                    ExecutionContext.HTTP_TARGET_HOST );
                if( authScheme != null ) {
                    Credentials creds = credsProvider.getCredentials(
                        new AuthScope(
                        targetHost.getHostName(),
                        targetHost.getPort() ) );
                    if( creds == null ) {
                        throw new HttpException( "No credentials for preemptive authentication" );
                    }
                    authState.setAuthScheme( authScheme );
                    authState.setCredentials( creds );
                }
            }

        }
    }
}
