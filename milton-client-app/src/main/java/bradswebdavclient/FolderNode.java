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

import com.ettrema.httpclient.Folder;
import com.ettrema.httpclient.FolderListener;
import com.ettrema.httpclient.Resource;
import com.ettrema.httpclient.ResourceListener;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.tree.MutableTreeNode;

/**
 *
 * @author mcevoyb
 */
public class FolderNode extends AbstractTreeNode implements Droppable, DeletableNode {

    public static final String ICON_FOLDER = "/s_folder.png";
    final Folder folder;
    FolderListener folderListener;

    public FolderNode( final AbstractTreeNode parent, Folder folder ) {
        super( parent, folder.name, false );
        if( folder == null ) throw new NullPointerException( "folder is null" );
        this.folder = folder;

        folder.addListener( new ResourceListener() {

            public void onChanged( Resource r ) {
                model().nodeChanged( FolderNode.this );
            }

            public void onDeleted( Resource r ) {
                System.out.println( "FolderNode: onDeleted: " + r.name );
                FolderNode fn = (FolderNode) FolderNode.this.getParent().child( FolderNode.this.folder.name );
                if( fn != null ) {
                    model().removeNodeFromParent( FolderNode.this );
                }
            }

            @Override
            public String toString() {
                return "FolderNode:ResourceListener:" + FolderNode.this.toString();
            }
        } );

        children = new ArrayList<AbstractTreeNode>();
    }

    @Override
    public void delete() {
        ResourceUtils.doDelete( App.current().getMainFrame(), folder );
    }

    private MutableTreeNode findNode( Resource r ) {
        if( children == null ) return null;
        for( AbstractTreeNode childNode : children ) {
            if( childNode instanceof FolderNode ) {
                FolderNode fn = (FolderNode) childNode;
                if( fn.folder == r ) return fn;
            }
        }
        return null;
    }

    public void select( String[] arr, int i ) {
        System.out.println( "select: " + folder.name + " -> " + i );
        if( i >= arr.length ) {
            System.out.println( "  show details" );
            select();
        } else {
            String s = arr[i];
            System.out.println( "  going to " + s );
            FolderNode child = (FolderNode) child( s );
            if( child != null ) {
                child.select( arr, i + 1 );
            }
        }
    }

    public void select() {
        selectThis();
        App.current().view.showDetails( createDetails() );
    }

    @Override
    public JPanel createDetails() {
        System.out.println( "FolderNode: creating panel" );
        try {
            return new FolderPanel( folder );
        } catch( Exception ex ) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog( App.current().getMainFrame(), "An error occured connecting to the host");
            return null;
        }
    }

    @Override
    protected String getIconName() {
        return ICON_FOLDER;
    }

    @Override
    protected void beforeFlush() {
        try {
            this.folder.flush();
        } catch( IOException ex ) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void flushChildren() {
        try {
            this.folder.flush();
        } catch( IOException ex ) {
            ex.printStackTrace();
        }
    }

    @Override
    protected final List<AbstractTreeNode> getChildren() {
        if( folderListener == null ) {
            try {
                folder.children();
                folderListener = new FolderNodeListener();
                folder.addListener( folderListener ); // activate folder population
            } catch( Exception ex ) {
                ex.printStackTrace();
            }
        }
        return children;
    }

    @Override
    protected List<AbstractTreeNode> listChildren() {
        throw new RuntimeException( "should never get called" );
    }

    @Override
    public Enumeration children() {
        return Collections.enumeration( getChildren() );
    }

    void createNewFolder( String name ) throws Exception {
        Folder newFolder = this.folder.createFolder( name );
    }

    @Override
    void updatePopupMenu( JPopupMenu popupMenu ) {
        super.updatePopupMenu( popupMenu );

        JMenuItem item = new JMenuItem( "New Folder" );
        item.addMouseListener( new NewFolderListener() );
        popupMenu.add( item );

        DownloadFolderListener.add( popupMenu, this );

    }

    class NewFolderListener extends AbstractMouseListener {

        @Override
        public void onClick() {
            String name = JOptionPane.showInputDialog( "New Folder Name" );
            if( name == null ) return;
            try {
                createNewFolder( name );
            } catch( Exception ex ) {
                ex.printStackTrace();
            }
        }
    }

    public DataFlavor[] getTransferDataFlavors() {
        System.out.println( "getTransferDataFlavours" );
        return null;
    }

    public boolean isDataFlavorSupported( DataFlavor flavor ) {
        System.out.println( "isDataFlavorSupported: " + flavor );
        return true;
    }

    public Object getTransferData( DataFlavor flavor ) throws UnsupportedFlavorException, IOException {
        System.out.println( "getTransferDate: " + flavor );
        return "heelo";
    }

    public boolean acceptCopyDrop( Transferable transferable ) {
        TransferableResourceList list = (TransferableResourceList) transferable;
        for( Resource r : list ) {
            try {
                r.copyTo( this.folder );
            } catch( Exception ex ) {
                ex.printStackTrace();
            }
        }
        return true;
    }

    public boolean canPerformMove( Transferable transferable ) {
        boolean b = true; //(transferable instanceof TransferableResourceList);
        return b;
    }

    public boolean canPerformCopy( Transferable transferable ) {
        return true;
//    return (transferable instanceof TransferableResourceList);
    }

    public boolean acceptMoveDrop( Transferable transferable ) {
        try {
            TransferableResourceList list = (TransferableResourceList) transferable.getTransferData( TransferableResourceList.RESOURCE_LIST_FLAVOR );
            for( Resource rSource : list ) {
                rSource.moveTo( this.folder );
            }
            return true;
        } catch( UnsupportedFlavorException ex ) {
            ex.printStackTrace();
        } catch( Exception ex ) {
            ex.printStackTrace();
        }
        return false;
    }

    class FolderNodeListener implements FolderListener {

        public void onChildAdded( Folder parent, Resource child ) {
            if( parent == FolderNode.this.folder ) {
                if( child instanceof Folder ) {
                    int num = numChildren();
                    FolderNode f = new FolderNode( FolderNode.this, (Folder) child );
                    model().insertNodeInto( f, FolderNode.this, num );
                }
            }
        }

        public void onChildRemoved( Folder parent, Resource child ) {
            if( parent == FolderNode.this.folder ) {
                MutableTreeNode node = findNode( child );
                if( node == null ) {
                    return;
                } else {
                    model().removeNodeFromParent( node );
                }
            }
            System.out.println( " done" );
        }

        @Override
        public String toString() {
            return "FolderNode:FolderListener:" + FolderNode.this.toString();
        }
    }
}
