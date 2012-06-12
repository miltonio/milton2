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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 *
 * @author mcevoyb
 */
public abstract class AbstractMouseListener implements MouseListener{
    
    protected final AbstractTreeNode node;
    
    public abstract void onClick();
    
    public AbstractMouseListener() {
        this.node = null;
    }

    public AbstractMouseListener(AbstractTreeNode node) {
        if( node == null ) throw new NullPointerException();
        this.node = node;
    }

    protected void doEvent() {
      onClick();
    }
    
    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
        doEvent();        
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}
