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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author mcevoyb
 */
public abstract class AbstractTreeNode implements MutableTreeNode, Comparable {

    public abstract JPanel createDetails();

    protected abstract String getIconName();
    protected final AbstractTreeNode parent;
    protected List<AbstractTreeNode> children;
    private String name;
    protected boolean isLeaf;

    AbstractTreeNode(AbstractTreeNode parent, String name, boolean isLeaf) {
        this.parent = parent;
        this.isLeaf = isLeaf;
        this.name = name;
    }

    ResourceTreeModel model() {
        return ((ResourceTreeModel) root().frame.tree().getModel());
    }

    int numChildren() {
        if (children == null) {
            return 0;
        }
        return children.size();
    }

    public String[] getPath() {
        ArrayList<String> list = new ArrayList<String>();
        AbstractTreeNode node = this;
        while (node != null) {
            list.add(0, node.toString());
            node = node.parent;
        }
        String[] arr = new String[list.size()];
        arr = list.toArray(arr);
        return arr;
    }

    protected List<AbstractTreeNode> listChildren() {
        return children;
    }

    protected void populateCellRenderer(MyCellRenderer renderer) {
        String iconName = getIconName();
        renderer.defaultCellRendering(iconName);
    }

    @Override
    public String toString() {
        return name;
    }

    protected void flushChildren() {
        System.out.println("flushChildren");
        flush(children);
        children = null;
        ((ResourceTreeModel) root().frame.tree().getModel()).nodeStructureChanged(this);
    }

    protected List<AbstractTreeNode> getChildren() {
        if (children == null) {
            children = listChildren();
            if (children == null) {
                children = new ArrayList<AbstractTreeNode>();
            }
            Collections.sort(children);
        }
        return children;
    }

    // MutableTreeNode - start
    public final boolean isLeaf() {
        return isLeaf;
    }

    public TreeNode getChildAt(int childIndex) {
        return getChildren().get(childIndex);
    }

    public int getChildCount() {
        return getChildren().size();
    }

    public AbstractTreeNode getParent() {
        return parent;
    }

    public int getIndex(TreeNode node) {
        List<? extends TreeNode> list = getChildren();
        for (int i = 0; i < list.size(); i++) {
            TreeNode n = list.get(i);
            if (n == node) {
                return i;
            }
        }
        return 0;
    }

    public boolean getAllowsChildren() {
        return !isLeaf();
    }

    public Enumeration children() {
        return Collections.enumeration(getChildren());
    }

    public void insert(MutableTreeNode child, int index) {
        System.out.println("inserting: " + child);
        List list = getChildren();
        if (list == null) {
            return;
        }
        list.add(index, child);
    }

    public void remove(int index) {
        List<? extends AbstractTreeNode> list = getChildren();
        if (list == null) {
            return;
        }
        if (list.size() > 0) {
            list.remove(index);
        }
    }

    public void remove(MutableTreeNode node) {
        List<? extends AbstractTreeNode> list = getChildren();
        if (list == null) {
            return;
        }
        list.remove(node);
    }

    public void setUserObject(Object object) {
    }

    public void removeFromParent() {
        getParent().getChildren().remove(this);
    }

    public void setParent(MutableTreeNode newParent) {
    }
    // MutableTreeNode - finish

    protected void flush(List<? extends AbstractTreeNode> list) {
        List<AbstractTreeNode> list2 = new ArrayList<AbstractTreeNode>();
        if (list != null) {
            list2.addAll(list);
            for (AbstractTreeNode n : list2) {
                flush(n);
            }
        }
    }

    protected void flush(AbstractTreeNode node) {
        if (node == null) {
            return;
        }
        node.beforeFlush();
        ((ResourceTreeModel) root().frame.tree().getModel()).removeNodeFromParent(node);
    }

    public final AbstractTreeNode getNode() {
        return this;
    }

    protected HostsNode root() {
        return parent.root();
    }

    public int compareTo(Object o) {
        if (o == null) {
            return -1;
        }
        if (o instanceof AbstractTreeNode) {
            AbstractTreeNode other = (AbstractTreeNode) o;
            if (!this.isLeaf()) {
                if (!other.isLeaf()) {
                    return compareNodeText(other);
                } else {
                    return -1;
                }
            } else {
                if (!other.isLeaf()) {
                    return 1;
                } else {
                    return compareNodeText(other);
                }
            }
        } else {
            return -1;
        }
    }

    protected int compareNodeText(AbstractTreeNode other) {
        return this.toString().compareTo(other.toString());
    }

    public boolean contains(String childName) {
        return child(childName) != null;
    }

    public AbstractTreeNode child(String childName) {
        for (AbstractTreeNode n : getChildren()) {
            if (n.toString().equals(childName)) {
                return n;
            }
        }
        return null;
    }

    public void selectThis() {
        System.out.println("selectThis");
        select(this);
    }

    public static void select(AbstractTreeNode node) {
        String[] path = node.getPath();
        TreePath tp = getTreePath(path);
        select(tp);
    }

    public static void select(TreePath tp) {
        if (tp == null) {
            return;
        }
        JTree tree = App.current().getFrame().tree();
        tree.setSelectionPath(tp);
        tree.scrollPathToVisible(tp);
    }

    public static TreePath getTreePath(String[] path) {
        JTree tree = App.current().getFrame().tree();
        AbstractTreeNode node = null;
        TreePath tp = null;
        for (String s : path) {
            if (node == null) {
                node = (AbstractTreeNode) tree.getModel().getRoot();
                tp = new TreePath(node);
            } else {
                node = node.child(s);
                if (node == null) {
                    System.out.println("Did not find child: " + s);
                    return null;
                }
                tp = tp.pathByAddingChild(node);
            }
        }
        return tp;
    }

    void updatePopupMenu(JPopupMenu popupMenu) {
        popupMenu.removeAll();
        RefreshMouseListener.add(popupMenu, this);
    }

    protected void beforeFlush() {
    }
}
