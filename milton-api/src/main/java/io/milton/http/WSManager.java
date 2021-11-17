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

package io.milton.http;

/**
 *
 */
public interface WSManager {

    /**
     * Notifies client that file/folder was created.
     *
     * @param itemPath file/folder.
     */
    void notifyCreated(String itemPath);

    /**
     * Notifies client that file/folder was updated.
     *
     * @param itemPath file/folder.
     */
    void notifyUpdated(String itemPath);

    /**
     * Notifies client that file/folder was deleted.
     *
     * @param itemPath file/folder.
     */
    void notifyDeleted(String itemPath);

    /**
     * Notifies client that file/folder was locked.
     *
     * @param itemPath file/folder.
     */
    void notifyLocked(String itemPath);

    /**
     * Notifies client that file/folder was unlocked.
     *
     * @param itemPath file/folder.
     */
    void notifyUnlocked(String itemPath);

    /**
     * Notifies client that file/folder was moved.
     *
     * @param itemPath file/folder.
     */
    void notifyMoved(String itemPath, String targetPath);

}

