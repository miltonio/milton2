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
import java.util.List;

/**
 *
 * @author mcevoyb
 */
public class TransferableRows implements Transferable {

    public static final DataFlavor NODE_FLAVOR = new DataFlavor( DataFlavor.javaJVMLocalObjectMimeType, "Row" );
    private static DataFlavor[] flavors = new DataFlavor[]{NODE_FLAVOR};
    final List<Resource> rows;

    public TransferableRows( List<Resource> rows ) {
        this.rows = rows;
    }

    public synchronized Object getTransferData( DataFlavor flavor ) throws UnsupportedFlavorException {
        if( flavor == NODE_FLAVOR ) {
            return rows;
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
