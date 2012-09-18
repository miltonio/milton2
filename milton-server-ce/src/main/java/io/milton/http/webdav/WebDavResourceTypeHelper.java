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

package io.milton.http.webdav;

import io.milton.resource.CollectionResource;
import io.milton.resource.LockableResource;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class WebDavResourceTypeHelper implements ResourceTypeHelper {

    private static final Logger log = LoggerFactory.getLogger( WebDavResourceTypeHelper.class );

	@Override
    public List<QName> getResourceTypes( Resource r ) {
        if( r instanceof CollectionResource ) {
            ArrayList<QName> list = new ArrayList<QName>();
            QName qn = new QName( WebDavProtocol.NS_DAV.getName(), "collection" );
            list.add( qn );
            return list;
        } else {
            return null;
        }
    }

    //Need to create a ArrayList as Arrays.asList returns a fixed length list which
    //cannot be extended.
	@Override
    public List<String> getSupportedLevels( Resource r ) {
        if( r instanceof LockableResource ) {
            return new ArrayList<String> (Arrays.asList( "1", "2" ));
        } else {
            return new ArrayList<String> (Arrays.asList( "1" ));
        }
    }
}
