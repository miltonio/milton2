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
