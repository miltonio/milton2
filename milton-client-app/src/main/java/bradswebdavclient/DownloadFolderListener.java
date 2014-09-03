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
import java.io.File;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;

/**
 *
 * @author mcevoyb
 */
public class DownloadFolderListener extends AbstractMouseListener{

  static void add(JPopupMenu popupMenu, FolderNode aThis) {
      JMenuItem item = new JMenuItem("Download to desktop");
      item.addMouseListener( new DownloadFolderListener(aThis.folder) );
      popupMenu.add(item);
  }

  final Folder folder;

  public DownloadFolderListener(Folder folder) {
    this.folder = folder;
  }
  
  
  
  @Override
  public void onClick() {
    Application app = BradsWebdavClientApp.getApplication();
    Task task = new DownloadFolderTask(app);
    BradsWebdavClientApp.getApplication().getContext().getTaskService().execute(task);
  }

  
  class DownloadFolderTask extends Task implements ProgressListener{

    DownloadFolderTask(Application app) {
      super(app);
    }

    @Override
    protected Object doInBackground() throws Exception {
      String dest = System.getProperty("user.home");      
      File fDest = new File(dest);
      if( fDest.exists() ) {
          File f2 = new File(fDest,"Desktop");
          if( f2.exists() ) fDest = f2;
      } else {
          throw new RuntimeException("Couldnt find user's home directory: " + dest);
      }
      System.out.println("downloading to: " + fDest.getAbsolutePath());
      folder.downloadTo(fDest, this);
      return null;
    }

    public void onProgress(int percent, String fileName) {
      App.current().view.status("Downloading: " + fileName);
      this.setProgress(percent);
    }

    public void onComplete(String fileName) {
      App.current().view.status("Finished: " + fileName);
      this.setProgress(100);
    }
    
  }
}
