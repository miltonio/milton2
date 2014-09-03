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
