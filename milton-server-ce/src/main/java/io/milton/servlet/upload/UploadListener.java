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

/* Licence:
 *   Use this however/wherever you like, just don't blame me if it breaks anything.
 *
 * Credit:
 *   If you're nice, you'll leave this bit:
 *
 *   Class by Pierre-Alexandre Losson -- http://www.telio.be/blog
 *   email : plosson@users.sourceforge.net
 */
package io.milton.servlet.upload;

public class UploadListener implements OutputStreamListener {

    private int totalBytesRead = 0;
    private int totalFiles = -1;
    
    public UploadListener( ) {
//        totalToRead = request.getContentLength();
//        this.startTime = System.currentTimeMillis();
    }
    
    public void start() {
        totalFiles ++;
        updateUploadInfo("start");
    }
    
    public void bytesRead(int bytesRead) {
        totalBytesRead = totalBytesRead + bytesRead;
        updateUploadInfo("progress");        
    }
    
    public void error(String message) {
        updateUploadInfo("error");
    }
    
    public void done() {
        updateUploadInfo("done");
    }
    
    private void updateUploadInfo(String status) {
//        long delta = (System.currentTimeMillis() - startTime) / 1000;
//        request.getSession().setAttribute("uploadInfo", new UploadInfo(totalFiles, totalToRead, totalBytesRead,delta,status));
    }
    
}
