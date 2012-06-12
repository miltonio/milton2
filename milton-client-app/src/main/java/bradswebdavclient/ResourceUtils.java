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
