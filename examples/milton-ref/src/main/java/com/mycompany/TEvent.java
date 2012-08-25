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

package com.mycompany;

import io.milton.http.Range;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.ICalResource;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class TEvent extends TResource implements ICalResource {

    private static final Logger log = LoggerFactory.getLogger( TEvent.class );
    private String iCalData;

    public TEvent( TFolderResource parent, String name ) {
        super( parent, name );
    }

    @Override
    protected Object clone( TFolderResource newParent, String newName ) {
        TEvent e = new TEvent( (TCalendarResource) newParent, newName );
        e.setiCalData( iCalData );
        return e;
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        out.write( iCalData.getBytes("UTF-8") );
    }

    @Override
    public String getContentType( String accepts ) {
        return "text/calendar";
    }

    @Override
    public String getICalData() {
        return iCalData;
    }

    public void setiCalData( String iCalData ) {
        this.iCalData = iCalData;
    }
}
