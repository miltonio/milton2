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
 *
 * @author mcevoyb
 */
public abstract class BaseDetailsPanel extends javax.swing.JPanel implements EventHandler{
    
    
        
    /** Textual representation of the type of object
     */
    protected String entityName;
            
    public abstract void populateScreen();
    
    
    public BaseDetailsPanel(String entityName) {
        this.entityName = entityName;
    }
        
    /** Must be called immediately after construction
     */     
    protected final void init() {        
        populateScreen();
    }

        
    public final JPanel getPanel() {
        return this;
    }    

    public String getTitle() {
        return entityName;
    }
    
    public final boolean onCancel() {
        populateScreen();
        return true;
    }        
}
