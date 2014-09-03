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

/*
 * BradsWebdavClientView.java
 */
package bradswebdavclient;

import java.awt.event.MouseEvent;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 * The application's main frame.
 */
public class BradsWebdavClientView extends FrameView {

    public BradsWebdavClientView( SingleFrameApplication app ) {
        super( app );

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger( "StatusBar.messageTimeout" );
        messageTimer = new Timer( messageTimeout, new ActionListener() {

            public void actionPerformed( ActionEvent e ) {
                statusMessageLabel.setText( "" );
            }
        } );
        messageTimer.setRepeats( false );
        int busyAnimationRate = resourceMap.getInteger( "StatusBar.busyAnimationRate" );
        for( int i = 0; i < busyIcons.length; i++ ) {
            busyIcons[i] = resourceMap.getIcon( "StatusBar.busyIcons[" + i + "]" );
        }
        busyIconTimer = new Timer( busyAnimationRate, new ActionListener() {

            public void actionPerformed( ActionEvent e ) {
                busyIconIndex = ( busyIconIndex + 1 ) % busyIcons.length;
                statusAnimationLabel.setIcon( busyIcons[busyIconIndex] );
            }
        } );
        idleIcon = resourceMap.getIcon( "StatusBar.idleIcon" );
        statusAnimationLabel.setIcon( idleIcon );
        progressBar.setVisible( false );

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor( getApplication().getContext() );
        taskMonitor.addPropertyChangeListener( new java.beans.PropertyChangeListener() {

            public void propertyChange( java.beans.PropertyChangeEvent evt ) {
                String propertyName = evt.getPropertyName();
                if( "started".equals( propertyName ) ) {
                    if( !busyIconTimer.isRunning() ) {
                        statusAnimationLabel.setIcon( busyIcons[0] );
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible( true );
                    progressBar.setIndeterminate( true );
                } else if( "done".equals( propertyName ) ) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon( idleIcon );
                    progressBar.setVisible( false );
                    progressBar.setValue( 0 );
                } else if( "message".equals( propertyName ) ) {
                    String text = (String) ( evt.getNewValue() );
                    statusMessageLabel.setText( ( text == null ) ? "" : text );
                    messageTimer.restart();
                } else if( "progress".equals( propertyName ) ) {
                    int value = (Integer) ( evt.getNewValue() );
                    progressBar.setVisible( true );
                    progressBar.setIndeterminate( false );
                    progressBar.setValue( value );
                }
            }
        } );

        initTree();


    }

    @Action
    public void showAboutBox() {
        if( aboutBox == null ) {
            JFrame mainFrame = BradsWebdavClientApp.getApplication().getMainFrame();
            aboutBox = new BradsWebdavClientAboutBox( mainFrame );
            aboutBox.setLocationRelativeTo( mainFrame );
        }
        BradsWebdavClientApp.getApplication().show( aboutBox );
    }

    void status( String msg ) {
        this.statusMessageLabel.setText( msg );
    }

    private void gotoHref( String s ) {
        ResourceTreeModel model = (ResourceTreeModel) this.tree().getModel();
        model.select( s );
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    mainPanel = new javax.swing.JPanel();
    jSplitPane1 = new javax.swing.JSplitPane();
    jScrollPane1 = new javax.swing.JScrollPane();
    tree = new javax.swing.JTree();
    details = new javax.swing.JPanel();
    addressBar = new javax.swing.JPanel();
    addressText = new javax.swing.JTextField();
    menuBar = new javax.swing.JMenuBar();
    javax.swing.JMenu fileMenu = new javax.swing.JMenu();
    javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
    javax.swing.JMenu helpMenu = new javax.swing.JMenu();
    javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
    bookmarksMenu = new javax.swing.JMenu();
    bookmarkThisMenuItem = new javax.swing.JMenuItem();
    jSeparator1 = new javax.swing.JSeparator();
    statusPanel = new javax.swing.JPanel();
    javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
    statusMessageLabel = new javax.swing.JLabel();
    statusAnimationLabel = new javax.swing.JLabel();
    progressBar = new javax.swing.JProgressBar();
    popupMenu = new javax.swing.JPopupMenu();

    mainPanel.setName("mainPanel"); // NOI18N
    mainPanel.setLayout(new java.awt.GridBagLayout());

    jSplitPane1.setDividerLocation(250);
    jSplitPane1.setMinimumSize(new java.awt.Dimension(300, 200));
    jSplitPane1.setName("jSplitPane1"); // NOI18N
    jSplitPane1.setPreferredSize(new java.awt.Dimension(400, 300));

    jScrollPane1.setName("jScrollPane1"); // NOI18N

    tree.setEditable(true);
    tree.setName("tree"); // NOI18N
    tree.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        treeMouseClicked(evt);
      }
    });
    tree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
      public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
        treeValueChanged(evt);
      }
    });
    tree.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
      public void mouseDragged(java.awt.event.MouseEvent evt) {
        treeMouseDragged(evt);
      }
    });
    tree.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyReleased(java.awt.event.KeyEvent evt) {
        treeKeyReleased(evt);
      }
    });
    jScrollPane1.setViewportView(tree);

    jSplitPane1.setLeftComponent(jScrollPane1);

    details.setAlignmentY(0.0F);
    details.setName("details"); // NOI18N
    details.setLayout(new javax.swing.BoxLayout(details, javax.swing.BoxLayout.Y_AXIS));
    jSplitPane1.setRightComponent(details);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    mainPanel.add(jSplitPane1, gridBagConstraints);

    addressBar.setMaximumSize(new java.awt.Dimension(32767, 20));
    addressBar.setMinimumSize(new java.awt.Dimension(0, 20));
    addressBar.setName("addressBar"); // NOI18N
    addressBar.setPreferredSize(new java.awt.Dimension(100, 20));
    addressBar.setLayout(new java.awt.BorderLayout());

    org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(bradswebdavclient.BradsWebdavClientApp.class).getContext().getResourceMap(BradsWebdavClientView.class);
    addressText.setText(resourceMap.getString("addressText.text")); // NOI18N
    addressText.setName("addressText"); // NOI18N
    addressText.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyTyped(java.awt.event.KeyEvent evt) {
        addressTextKeyTyped(evt);
      }
    });
    addressBar.add(addressText, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    mainPanel.add(addressBar, gridBagConstraints);

    menuBar.setName("menuBar"); // NOI18N

    fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
    fileMenu.setName("fileMenu"); // NOI18N

    javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(bradswebdavclient.BradsWebdavClientApp.class).getContext().getActionMap(BradsWebdavClientView.class, this);
    exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
    exitMenuItem.setName("exitMenuItem"); // NOI18N
    fileMenu.add(exitMenuItem);

    menuBar.add(fileMenu);

    helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
    helpMenu.setName("helpMenu"); // NOI18N

    aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
    aboutMenuItem.setName("aboutMenuItem"); // NOI18N
    helpMenu.add(aboutMenuItem);

    menuBar.add(helpMenu);

    bookmarksMenu.setText(resourceMap.getString("bookmarksMenu.text")); // NOI18N
    bookmarksMenu.setName("bookmarksMenu"); // NOI18N

    bookmarkThisMenuItem.setText(resourceMap.getString("bookmarkThisMenuItem.text")); // NOI18N
    bookmarkThisMenuItem.setName("bookmarkThisMenuItem"); // NOI18N
    bookmarkThisMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        bookmarkThisMenuItemMouseClicked(evt);
      }
      public void mousePressed(java.awt.event.MouseEvent evt) {
        bookmarkThisMenuItemMousePressed(evt);
      }
    });
    bookmarksMenu.add(bookmarkThisMenuItem);

    jSeparator1.setName("jSeparator1"); // NOI18N
    bookmarksMenu.add(jSeparator1);

    menuBar.add(bookmarksMenu);

    statusPanel.setName("statusPanel"); // NOI18N

    statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

    statusMessageLabel.setName("statusMessageLabel"); // NOI18N

    statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

    progressBar.setValue(50);
    progressBar.setName("progressBar"); // NOI18N
    progressBar.setStringPainted(true);

    org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
    statusPanel.setLayout(statusPanelLayout);
    statusPanelLayout.setHorizontalGroup(
      statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
      .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 663, Short.MAX_VALUE)
      .add(statusPanelLayout.createSequentialGroup()
        .addContainerGap()
        .add(statusMessageLabel)
        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 479, Short.MAX_VALUE)
        .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
        .add(statusAnimationLabel)
        .addContainerGap())
    );
    statusPanelLayout.setVerticalGroup(
      statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
      .add(statusPanelLayout.createSequentialGroup()
        .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
          .add(statusMessageLabel)
          .add(statusAnimationLabel)
          .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        .add(3, 3, 3))
    );

    popupMenu.setName("popupMenu"); // NOI18N

    setComponent(mainPanel);
    setMenuBar(menuBar);
    setStatusBar(statusPanel);
  }// </editor-fold>//GEN-END:initComponents
  private void treeMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeMouseDragged
  }//GEN-LAST:event_treeMouseDragged

  private void treeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeValueChanged
  }//GEN-LAST:event_treeValueChanged

  private void treeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeMouseClicked
      final int clicks = evt.getClickCount();
      TreePath tp = tree.getSelectionPath();
      if( tp != null ) {
          if( clicks == 1 ) {
              selectNode( tp );
          } else if( clicks == 2 ) {
              selectNode( tp );
          }
      }
  }//GEN-LAST:event_treeMouseClicked

  private void addressTextKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addressTextKeyTyped
      System.out.println( "key text: " + (int) evt.getKeyChar() );
      if( (int) evt.getKeyChar() == 10 ) {
          String s = addressText.getText();
          gotoHref( s );
      }
  }//GEN-LAST:event_addressTextKeyTyped

  private void treeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_treeKeyReleased
      if( !ResourceUtils.isDeleteKey( evt.getKeyChar() ) ) return;

      TreePath tp = tree.getSelectionPath();
      AbstractTreeNode tn = (AbstractTreeNode) tp.getLastPathComponent();
      if( tn == null ) return;
      if( tn instanceof DeletableNode ) {
          ( (DeletableNode) tn ).delete();
      }
  }//GEN-LAST:event_treeKeyReleased

  private void bookmarkThisMenuItemMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bookmarkThisMenuItemMouseClicked
      System.out.println( "bookmarkThisMenuItemMouseClicked" );

}//GEN-LAST:event_bookmarkThisMenuItemMouseClicked

  private void bookmarkThisMenuItemMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bookmarkThisMenuItemMousePressed
      System.out.println( "mouse pressed" );
      if( currentDetails == null ) return;
      if( currentDetails instanceof Addressable ) {
          System.out.println( "  is add" );
          Addressable add = (Addressable) currentDetails;
          String href = add.getHref();
          JMenuItem item = new JMenuItem( href );
          item.addMouseListener( new BookmarkListener( href ) );
          bookmarksMenu.add( item );
          System.out.println( "  done" );
      }
  }//GEN-LAST:event_bookmarkThisMenuItemMousePressed

    private void selectNode( TreePath tp ) {
        AbstractTreeNode tn = (AbstractTreeNode) tp.getLastPathComponent();
        JPanel selectedDetails = tn.createDetails();
        if( selectedDetails != null ) {
            showDetails( selectedDetails );
        }
    }

    public void showDetails( JPanel panel ) {
        if( currentDetails != null ) {
            if( currentDetails instanceof Unloadable ) {
                ( (Unloadable) currentDetails ).unload();
            }
        }
        details.removeAll();
        details.add( new JPanel() );
        details.validate();

        if( panel != null ) {
            if( panel instanceof Addressable ) {
                Addressable add = (Addressable) panel;
                addressText.setText( add.getHref() );
            }
            details.add( panel );
            panel.setVisible( true );
            details.validate();
            currentDetails = panel;
        }
    }

    private void initTree() {
        ResourceTreeModel model = ResourceTreeModel.create( this );
        tree.setModel( model );
        tree.setCellRenderer( new MyCellRenderer() );

        tree.addMouseListener( new MouseListener() {

            public void mouseClicked( MouseEvent e ) {
                checkPopup( e );
            }

            public void mousePressed( MouseEvent e ) {
                checkPopup( e );
            }

            public void mouseReleased( MouseEvent e ) {
                checkPopup( e );
            }

            public void mouseEntered( MouseEvent e ) {
            }

            public void mouseExited( MouseEvent e ) {
            }

            void checkPopup( MouseEvent e ) {
                if( e.isPopupTrigger() ) {
                    JTree tree = (JTree) e.getSource();
                    int row = tree.getRowForLocation( e.getX(), e.getY() );
                    TreePath path = tree.getPathForLocation( e.getX(), e.getY() );
                    if( path != null ) {
                        AbstractTreeNode node = (AbstractTreeNode) path.getLastPathComponent();
                        if( node != null ) {
                            node.updatePopupMenu( popupMenu );
                            popupMenu.show( tree, e.getX(), e.getY() );
                        }
                    }
                }
            }
        } );

        treeHandler = new TreeResourceTransferHandler( tree );
    }

    JTree tree() {
        return tree;
    }

    class BookmarkListener implements MouseListener {

        final String href;

        public BookmarkListener( String href ) {
            this.href = href;
        }

        public void mouseClicked( MouseEvent e ) {
        }

        public void mousePressed( MouseEvent e ) {
            gotoHref( href );
        }

        public void mouseReleased( MouseEvent e ) {
        }

        public void mouseEntered( MouseEvent e ) {
        }

        public void mouseExited( MouseEvent e ) {
        }
    }
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel addressBar;
  private javax.swing.JTextField addressText;
  private javax.swing.JMenuItem bookmarkThisMenuItem;
  private javax.swing.JMenu bookmarksMenu;
  private javax.swing.JPanel details;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JSeparator jSeparator1;
  private javax.swing.JSplitPane jSplitPane1;
  private javax.swing.JPanel mainPanel;
  private javax.swing.JMenuBar menuBar;
  private javax.swing.JPopupMenu popupMenu;
  private javax.swing.JProgressBar progressBar;
  private javax.swing.JLabel statusAnimationLabel;
  private javax.swing.JLabel statusMessageLabel;
  private javax.swing.JPanel statusPanel;
  private javax.swing.JTree tree;
  // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
    private JPanel currentDetails;
    TreeResourceTransferHandler treeHandler;
}
