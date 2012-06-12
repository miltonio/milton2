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
