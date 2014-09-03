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
import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author mcevoyb
 */
public class ResourceUtils {

    public static boolean isDeleteKey( int keyChar ) {
        return ( keyChar == 127 );
    }

    public static void doDelete( Component component, int keyChar, List<Resource> toDelete ) {
        if( !isDeleteKey( keyChar ) ) return;

        doDelete( component, toDelete );
    }

    public static void doDelete( Component component, Resource toDelete ) {
        List<Resource> list = new ArrayList<Resource>();
        list.add( toDelete );
        doDelete( component, list );
    }

    public static void doDelete( Component component, List<Resource> toDelete ) {
        if( toDelete.isEmpty() ) {
            return;
        }

        if( toDelete.size() == 1 ) {
            Resource r = toDelete.get( 0 );
            int result = JOptionPane.showConfirmDialog( component, "Are you sure you want to delete: " + r.href() + "?" );
            if( result == JOptionPane.YES_OPTION ) {
                try {
                    r.delete();
                } catch( Exception ex ) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog( component, "An error occurred deleting the resource");
                }
            } else {
                return;
            }
        } else {
            int result = JOptionPane.showConfirmDialog( component, "Are you sure you want to delete: " + toDelete.size() + " items?" );
            if( result == JOptionPane.YES_OPTION ) {
                for( Resource r : toDelete ) {
                    try {
                        r.delete();
                    } catch( Exception ex ) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog( component, "An error occurred deleting the resource");
                    }
                }
            } else {
                return;
            }
        }
    }
}
