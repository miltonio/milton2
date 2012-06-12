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

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author mcevoyb
 */
public class RefreshMouseListener extends AbstractMouseListener {

    public static void add(JPopupMenu popup, AbstractTreeNode node) {
        JMenuItem item = new JMenuItem("Refresh");
        item.addMouseListener( new RefreshMouseListener(node) );
        popup.add(item);
    }
        
    public RefreshMouseListener(AbstractTreeNode node) {
        super(node);
    }
    
    public void onClick() {
        node.flushChildren();
        DefaultTreeModel model = (DefaultTreeModel) App.current().getFrame().tree().getModel();
        model.nodeStructureChanged(node);
    }    
}
