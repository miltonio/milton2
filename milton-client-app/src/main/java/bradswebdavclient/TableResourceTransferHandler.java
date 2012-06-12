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

import com.ettrema.httpclient.Resource;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import javax.swing.JComponent;
import javax.swing.JTable;

/**
 *
 * @author mcevoyb
 */
public class TableResourceTransferHandler extends ResourceTransferHandler {

    private static final long serialVersionUID = 1L;
    final Droppable dropTarget;

    public TableResourceTransferHandler( JTable table, Droppable dropTarget ) {
        super( table );
        this.dropTarget = dropTarget;
//    table.setDragEnabled(true);
//    table.setDropMode(DropMode.ON_OR_INSERT);
    }

    JTable table() {
        return (JTable) this.component;
    }

    @Override
    protected TransferableResourceList selectedResources() {
        int[] rows = table().getSelectedRows();
        TransferableResourceList list = new TransferableResourceList();
        FolderPanel.FolderModel sourceModel = (FolderPanel.FolderModel) table().getModel();
        for( int rowNum : rows ) {
            Resource r = sourceModel.getResource( rowNum );
            list.add( r );
        }
        return list;
    }

    @Override
    protected boolean canPerformAction( JComponent target, Transferable transferable, int action, Point location ) {
        System.out.println( "canPerformAction: " + action );
        if( action == DnDConstants.ACTION_COPY ) {
            return dropTarget.canPerformCopy( transferable );
        } else if( action == DnDConstants.ACTION_MOVE ) {
            return dropTarget.canPerformMove( transferable );
        } else {
            return false;
        }
    }

    @Override
    protected boolean executeDrop( Transferable transferable, Point pt, int action ) {
        if( action == DnDConstants.ACTION_COPY ) {
            if( dropTarget.acceptCopyDrop( transferable ) ) {
                return true;
            } else {
                return false;
            }
        } else if( action == DnDConstants.ACTION_MOVE ) {
            if( dropTarget.acceptMoveDrop( transferable ) ) {
                return true;
            } else {
                return false;
            }
        } else {
            throw new RuntimeException( "unknown action: " + action );
        }
    }
}
