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

import com.ettrema.httpclient.ConnectionListener;
import com.ettrema.httpclient.Host;
import java.awt.Cursor;
import java.io.IOException;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author mcevoyb
 */
public class HostNode extends FolderNode {

    final Host host;

    public HostNode( AbstractTreeNode parent, Host host ) throws Exception {
        super( parent, host.getFolder( "" ) );
        this.host = host;
        host.connectionListeners.add( new ConnectionListener() {

            public void onStartRequest() {
                Cursor hourglassCursor = new Cursor( Cursor.WAIT_CURSOR );
                App.current().getFrame().getComponent().setCursor( hourglassCursor );
            }

            public void onFinishRequest() {
                Cursor normalCursor = new Cursor( Cursor.DEFAULT_CURSOR );
                App.current().getFrame().getComponent().setCursor( normalCursor );
            }
        } );
    }

    @Override
    protected String getIconName() {
        return "home.png";
    }

    void select( String path ) {
        System.out.println( "select: " + path );
        if( path.startsWith( "/" ) ) {
            path = path.substring( 1 );
        }
        if( path.endsWith( "/" ) ) {
            path = path.substring( 0, path.length() - 1 );
        }
        path = path.trim();
        if( path.length() == 0 ) {
            select();
        } else {
            System.out.println( "path=" + path );
            String[] arr = path.split( "[/]" );
            for( String s : arr ) {
                System.out.println( " arr=" + s );
            }
            System.out.println( "  selecting: " + arr.length );
            select( arr, 0 );
        }
    }

    @Override
    void updatePopupMenu( JPopupMenu popupMenu ) {
        super.updatePopupMenu( popupMenu );

        JMenuItem item = new JMenuItem( "Remove Host" );
        item.addMouseListener( new AbstractMouseListener( this ) {

            @Override
            public void onClick() {
                parent.remove( HostNode.this );
            }
        } );
        popupMenu.add( item );
    }

    @Override
    public String toString() {
        return host.server;
    }
}


