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
