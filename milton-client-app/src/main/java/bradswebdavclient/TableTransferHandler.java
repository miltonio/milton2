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

import com.ettrema.httpclient.Resource;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.util.List;
import javax.swing.JTable;
import javax.swing.TransferHandler;

/**
 *
 * @author mcevoyb
 */
public class TableTransferHandler extends TransferHandler implements DragGestureListener, DragSourceListener {

    private static TableTransferHandler theInstance;
    private static final long serialVersionUID = 1L;
    private final DragSource dragSource;
    private static List<Resource> draggedRows;
    private DropTarget dt;
    private final JTable table;
    private final FolderPanel.FolderModel sourceModel;

    static TableTransferHandler initInstance( JTable table, FolderPanel.FolderModel model ) {
        theInstance = new TableTransferHandler( table, model, DnDConstants.ACTION_COPY_OR_MOVE );
        table.setTransferHandler( theInstance );
        return theInstance;
    }

    private TableTransferHandler( JTable table, FolderPanel.FolderModel model, int action ) {
        this.table = table;
        this.sourceModel = model;
        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer( table, action, this );
//    dt = new DropTarget(table, this);
    }

    public void dragDropEnd( DragSourceDropEvent dsde ) {
        if( dsde.getDropSuccess() && dsde.getDropAction() == DnDConstants.ACTION_MOVE ) {
            sourceModel.fireTableDataChanged();
        }
    }

    public final void dragEnter( DragSourceDragEvent dsde ) {
        int action = dsde.getDropAction();
        if( action == DnDConstants.ACTION_COPY ) {
            dsde.getDragSourceContext().setCursor( DragSource.DefaultCopyDrop );
        } else {
            if( action == DnDConstants.ACTION_MOVE ) {
                dsde.getDragSourceContext().setCursor( DragSource.DefaultMoveDrop );
            } else {
                dsde.getDragSourceContext().setCursor( DragSource.DefaultMoveNoDrop );
            }
        }
    }

    public final void dragOver( DragSourceDragEvent dsde ) {
        int action = dsde.getDropAction();
        if( action == DnDConstants.ACTION_COPY ) {
            dsde.getDragSourceContext().setCursor( DragSource.DefaultCopyDrop );
        } else {
            if( action == DnDConstants.ACTION_MOVE ) {
                dsde.getDragSourceContext().setCursor( DragSource.DefaultMoveDrop );
            } else {
                dsde.getDragSourceContext().setCursor( DragSource.DefaultMoveNoDrop );
            }
        }
    }

    public final void dropActionChanged( DragSourceDragEvent dsde ) {
        int action = dsde.getDropAction();
        if( action == DnDConstants.ACTION_COPY ) {
            dsde.getDragSourceContext().setCursor( DragSource.DefaultCopyDrop );
        } else {
            if( action == DnDConstants.ACTION_MOVE ) {
                dsde.getDragSourceContext().setCursor( DragSource.DefaultMoveDrop );
            } else {
                dsde.getDragSourceContext().setCursor( DragSource.DefaultMoveNoDrop );
            }
        }
    }

    public final void dragExit( DragSourceEvent dse ) {
        dse.getDragSourceContext().setCursor( DragSource.DefaultMoveNoDrop );
    }

    /* Methods for DragGestureListener */
    public final void dragGestureRecognized( DragGestureEvent dge ) {
        System.out.println( "dragGestureRecognized" );
        draggedRows = sourceModel.getSelectedResources();
        if( draggedRows != null && draggedRows.size() > 0 ) {
            System.out.println( "start drag" );
            TransferableResourceList list = new TransferableResourceList( draggedRows );
            dragSource.startDrag( dge, DragSource.DefaultMoveNoDrop, list, this );
        }
    }

    /* Methods for DropTargetListener */
//  public final void dragEnter(DropTargetDragEvent dtde) {
//    System.out.println("dragEnter(target): " + dtde + " - " + System.currentTimeMillis());
//    Point pt = dtde.getLocation();
//    int action = dtde.getDropAction();
////    if (canPerformAction(tree, draggedNode, action, pt)) {
//    dtde.acceptDrag(action);
////        } else {
////            dtde.rejectDrag();
////        }
//  }
//  public final void dragExit(DropTargetEvent dte) {
//    System.out.println("dragExit(target)");
//  }
//
//  public final void dragOver(DropTargetDragEvent dtde) {
//    Point pt = dtde.getLocation();
//    int action = dtde.getDropAction();
//    if (canPerformAction(table, draggedNode, action, pt)) {
//      dtde.acceptDrag(action);
//    } else {
//      dtde.rejectDrag();
//    }
//  }
//
//  public final void dropActionChanged(DropTargetDragEvent dtde) {
//    Point pt = dtde.getLocation();
//    int action = dtde.getDropAction();
////        if (canPerformAction(tree, draggedNode, action, pt)) {
//    dtde.acceptDrag(action);
////        } else {
////            dtde.rejectDrag();
////        }
//  }
//  public final void drop(DropTargetDropEvent dtde) {
//    System.out.println("drop");
//    try {
//      int action = dtde.getDropAction();
//      Transferable transferable = dtde.getTransferable();
//      Point pt = dtde.getLocation();
////            if (transferable.isDataFlavorSupported(TransferableNode.NODE_FLAVOR) && canPerformAction(tree, draggedNode, action, pt)) {
//      TreePath pathTarget = table.getPathForLocation(pt.x, pt.y);
//      AbstractTreeNode node = (AbstractTreeNode) transferable.getTransferData(TransferableNode.NODE_FLAVOR);
//      AbstractTreeNode newParentNode = (AbstractTreeNode) pathTarget.getLastPathComponent();
//      if (executeDrop(tree, node, newParentNode, action)) {
//        dtde.acceptDrop(action);
//        dtde.dropComplete(true);
//        return;
//      }
////            }
////            dtde.rejectDrop();
////            dtde.dropComplete(false);
//    } catch (Exception e) {
//      System.out.println(e);
//      dtde.rejectDrop();
//      dtde.dropComplete(false);
//    }
//  }
//
//
//  public boolean executeDrop(final JTree target, final AbstractTreeNode draggedNode, final AbstractTreeNode newParentNode, final int action) {
//    System.out.println("executeDrop");
//    if (action == DnDConstants.ACTION_COPY) {
//      final Droppable d = (Droppable) newParentNode;
//      System.out.println("droppable: " + d);
//      System.out.println("draggedNode: " + draggedNode);
//      if (d.acceptCopyDrop(draggedNode)) {
//        return true;
//      } else {
//        return false;
//      }
//
//    } else if (action == DnDConstants.ACTION_MOVE) {
//      final Droppable d = (Droppable) newParentNode;
//      System.out.println("droppable: " + d);
//      System.out.println("draggedNode: " + draggedNode);
//      if (d.acceptMoveDrop(draggedNode)) {
//        draggedNode.removeFromParent();
//        return true;
//      } else {
//        return false;
//      }
//    } else {
//      return false;
//    }
//  }
}
