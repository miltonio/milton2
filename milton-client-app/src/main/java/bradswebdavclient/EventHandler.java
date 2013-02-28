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

import javax.swing.JPanel;

/**
 * Defines an event handler for a BuildDialog events.
 * @author hollomg
 */
public interface EventHandler {
 
  /**   Return the panel with the UI for this dialog. Usually, just return this
   *
   *    Should register any drop target components with the transfer handler
   */
  JPanel getPanel();
  
  /**   return the text of the title for a dialog box which will show these details
   */
  public String getTitle();
}
