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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.List;

public class TransferableResourceList extends ArrayList<Resource> implements Transferable {

//  static DataFlavor uriListFlavor;
//
//  static {
//    try {
//      uriListFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
//    } catch (ClassNotFoundException e) { // can't happen
//      e.printStackTrace();
//    }
//  }  
    public static final DataFlavor RESOURCE_LIST_FLAVOR = new DataFlavor( DataFlavor.javaJVMLocalObjectMimeType, "ResourceList" );
    private static DataFlavor[] flavors = new DataFlavor[]{RESOURCE_LIST_FLAVOR};//, DataFlavor.javaFileListFlavor, uriListFlavor};
    private static final long serialVersionUID = 1L;

    public TransferableResourceList() {
    }

    public TransferableResourceList( Resource resource ) {
        super();
        this.add( resource );
    }

    public TransferableResourceList( List<Resource> resources ) {
        super( resources );
    }

    public synchronized Object getTransferData( DataFlavor flavor ) throws UnsupportedFlavorException {
        System.out.println( "getTransferData: " + flavor );
        if( flavor == RESOURCE_LIST_FLAVOR ) {
            return this;
//    } else if (flavor.equals(DataFlavor.javaFileListFlavor)) {
//      java.util.List data = new java.util.ArrayList();
//      java.io.File file = new java.io.File("/home/j2ee/Desktop/beergroup");
//      data.add(file);
//      return data;
//    } else if (flavor.equals(uriListFlavor)) {
//      java.io.File file = new java.io.File("/home/j2ee/Desktop/beergroup");
//      // refer to RFC 2483 for the text/uri-list format
//      String data = file.toURI() + "\r\n";
//      System.out.println("returning files list: " + data);
//      return data;
        } else {
            throw new UnsupportedFlavorException( flavor );
        }
    }

    public DataFlavor[] getTransferDataFlavors() {
        return flavors.clone();
    }

    public boolean isDataFlavorSupported( DataFlavor flavor ) {
        for( int i = 0; i < flavors.length; i++ ) {
            if( flavor.equals( flavors[i] ) ) {
                return true;
            }
        }
        return false;
    }
}
