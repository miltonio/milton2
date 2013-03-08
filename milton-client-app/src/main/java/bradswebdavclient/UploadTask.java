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

package bradswebdavclient;

import com.ettrema.httpclient.Folder;
import com.ettrema.httpclient.ProgressListener;
import com.ettrema.httpclient.Throttle;
import java.awt.Component;
import javax.swing.JOptionPane;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;

/**
 *
 * @author mcevoyb
 */
public class UploadTask extends Task implements ProgressListener {

    final Component parent;
    final java.io.File[] files;
    final Folder folder;

    public UploadTask( Component parent, Application app, java.io.File[] files, Folder folder ) {
        super( app );
        this.parent = parent;
        this.files = files;
        this.folder = folder;
        this.setUserCanCancel( true );
    }

    @Override
    protected Object doInBackground() throws Exception {
        System.out.println( "doInBackground" );
        int numFiles = files.length;
        int currentFile = 1;
        for( java.io.File f : files ) {
            System.out.println( "Upload::" + f.getName() );
            if( f.getName().startsWith( "." ) || f.getParentFile().getName().startsWith( "." ) ) {
                System.out.println( "not uploading: " + f.getName() );
            } else {
                try {
                    folder.upload( f, this, new Throttle() {

                        public void onRead( int len ) {
                        }
                    } );
                    currentFile++;
                } catch( Throwable e ) {
                    e.printStackTrace();
                    String msg = "Failed to upload: " + f.getAbsolutePath() + ". Error: " + e.getMessage();
                    if( currentFile >= numFiles ) {
                        JOptionPane.showMessageDialog( parent, msg, "Upload Failed", JOptionPane.WARNING_MESSAGE );
                    } else {
                        int res = JOptionPane.showConfirmDialog( parent, msg + ". Would you like to continue?", "Upload failed", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE );
                        if( res != JOptionPane.YES_OPTION ) {
                            return null;
                        }
                    }
                }
            }
        }
        App.current().view.status( "Completed uploading file(s)" );
        return null;
    }

    public void onProgress( int percent, String fileName ) {
        App.current().view.status( "Uploading: " + fileName );
        this.setProgress( percent );
    }

    public void onComplete( String fileName ) {
        App.current().view.status( "Finished: " + fileName );
        this.setProgress( 100 );
    }
}
