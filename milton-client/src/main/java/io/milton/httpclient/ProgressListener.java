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

/**
 * Use this for three purposes
 *	a. implement a bandwidth throttle by putting a delay in onRead
 *  b. display progress information to the user in onProgress and onComplete
 *  c. allow the user to cancel transfers by implementing isCancelled
 *
 * @author bradm
 */
public interface ProgressListener {

	/**
	 * Called on every read operation. If you implement any logic in here
	 * is must be fast!
	 * 
	 * @param bytes - the number of bytes read
	 */
	void onRead(int bytes);
	
	/**
	 * Called occasionally, after a reasonable period has passed so is suitable
	 * for GUI updates
	 * 
	 * @param bytesRead
	 * @param totalBytes - is null if unknown
	 * @param fileName - name of the file being transferred
	 */
    void onProgress( long bytesRead, Long totalBytes, String fileName );

	/**
	 * Called on completion
	 * 
	 * @param fileName 
	 */
    void onComplete( String fileName );

    /**
     * This is a means for the UI to inform that process that the user has
     * cancelled the optioation
	 * 
	 * If the implementation returns true the operation will abort
     *
     * @return
     */
    boolean isCancelled();
}
