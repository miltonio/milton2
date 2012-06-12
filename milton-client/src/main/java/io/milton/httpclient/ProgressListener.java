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
