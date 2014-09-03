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

import java.awt.Component;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author mcevoyb
 */
public class MyCellRenderer extends DefaultTreeCellRenderer {
    
   
           
    public static Icon load(String s) {        
        InputStream in = MyCellRenderer.class.getResourceAsStream(s);
        if( in == null ) {
            System.out.println("Could not load: " + s);
            return null;
        }
        MyByteArrayOutputStream out = new MyByteArrayOutputStream();
        out.read(in);
        return new ImageIcon(out.toByteArray());
    }
    
    private static Map<String,Icon> icons = new HashMap<String,Icon>();
    
    public static synchronized Icon getIcon(String name) {
        Icon ic = icons.get(name);
        if( ic == null ) {
            ic = load(name);
            icons.put(name,ic);
        }
        return ic;
    }
        
    
  @Override
    public Component getTreeCellRendererComponent(   JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {
        Component c = super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
        
        AbstractTreeNode node = (AbstractTreeNode) value;
        
        node.populateCellRenderer(this);
                
        return this;
    }
    
    public void defaultCellRendering(String iconName) {
        if( !iconName.startsWith("/") ) iconName = "/" + iconName;
        Icon ic = getIcon(iconName);
        setIcon(ic);
    }
    
}
