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

import com.ettrema.httpclient.Host;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.tree.MutableTreeNode;

/**
 *
 * @author j2ee
 */
public class HostsNode extends AbstractTreeNode {

    BradsWebdavClientView frame;

    public HostsNode(BradsWebdavClientView frame) {
        super(null, "Hosts", false);
        this.frame = frame;
    }

    @Override
    protected List<AbstractTreeNode> listChildren() {
        FileInputStream inStream = null;
        List<AbstractTreeNode> ch = new ArrayList<AbstractTreeNode>();
        try {
            Properties props = new Properties();
            File fHosts = getConfigFile();
            if (fHosts.exists()) {
                inStream = new FileInputStream(fHosts);
                if (inStream != null) {
                    props.load(inStream);
                    Enumeration e = props.propertyNames();
                    while (e.hasMoreElements()) {
                        String k = (String) e.nextElement();
                        String v = props.getProperty(k);
                        HostNode h = fromString(v);
                        ch.add(h);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("no config file");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException ex) {
            }
        }
        return ch;
    }

    private File getConfigFile() {
        File fHome = new File(System.getProperty("user.home"));
        File fConfig = new File(fHome, ".webdave");
        if (!fConfig.exists()) {
            fConfig.mkdir();
        }
        File fHosts = new File(fConfig, "hosts.txt");
        return fHosts;
    }

    @Override
    public JPanel createDetails() {
        return null;
    }

    @Override
    protected String getIconName() {
        return "network.png";
    }

    @Override
    protected HostsNode root() {
        return this;
    }

    void select(String s) {
        System.out.println("HostsNode: select: " + s);
        try {
            URL url = new URL(s);
            String hostName = url.getHost();
            HostNode node = (HostNode) this.child(hostName);
            System.out.println("  host: " + hostName + " - " + node);
            node.select(url.getPath());
        } catch (MalformedURLException ex) {
            throw new RuntimeException(s, ex);
        }
    }

    @Override
    void updatePopupMenu(JPopupMenu popupMenu) {
        super.updatePopupMenu(popupMenu);

        JMenuItem item = new JMenuItem("New Host");
        item.addMouseListener(new NewHostListener());
        popupMenu.add(item);
    }

    public void storeConfig() {
        File fHosts = getConfigFile();
        FileOutputStream out = null;
        try {
            Properties props = new Properties();
            storeHostProps(props);
            out = new FileOutputStream(fHosts);
            props.store(out, null);
            out.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    void storeHostProps(Properties props) {
        for (AbstractTreeNode node : getChildren()) {
            HostNode h = (HostNode) node;
            String v = toString(h);
            props.put("host." + h.host.server, v);
        }
    }

    String toString(HostNode h) {
        return h.host.server + "," + h.host.port + "," + h.host.user + "," + h.host.password;
    }

    HostNode fromString(String hostConfig) {
        String[] arr = hostConfig.split(",");
        String server = arr[0];
        int port = Integer.parseInt(arr[1]);
        String user = arr[2];
        String password = arr[3];
        Host h = new Host(server, port, user, password, null);
        try {
            return new HostNode(this, h);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void remove(MutableTreeNode node) {
        super.remove(node);
        getChildren().remove(node);
        HostsNode.this.storeConfig();
        ((ResourceTreeModel) root().frame.tree().getModel()).removeNodeFromParent(node);
    }

    class NewHostListener extends AbstractMouseListener {

        @Override
        public void onClick() {
            String hostName = JOptionPane.showInputDialog("Host name");
            if (hostName == null) {
                return;
            }
            String sPort = JOptionPane.showInputDialog("Host port");
            if (sPort == null) {
                sPort = "80";
            }
            int port = Integer.parseInt(sPort);
            String username = JOptionPane.showInputDialog("User name");
            if (username == null) {
                return;
            }
            String password = JOptionPane.showInputDialog("Password");
            if (password == null) {
                return;
            }
            String rootPath = JOptionPane.showInputDialog("Path");
            if (password == null) {
                return;
            }

            Host h = new Host(hostName,rootPath, port, username, password, null, null);
            HostNode hn;
            try {
                hn = new HostNode(HostsNode.this, h);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame.getComponent(), "An error occured connecting to the host");
                return;
            }
            HostsNode.this.getChildren().add(hn);
            HostsNode.this.storeConfig();
            ((ResourceTreeModel) root().frame.tree().getModel()).insertNodeInto(hn, HostsNode.this, 0);
        }
    }
}
