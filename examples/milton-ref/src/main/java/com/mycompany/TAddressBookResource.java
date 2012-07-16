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

import io.milton.common.InternationalizedString;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.values.AddressDataTypeList;
import io.milton.http.values.Pair;
import io.milton.resource.AddressBookResource;
import io.milton.resource.ReportableResource;
import io.milton.resource.Resource;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class TAddressBookResource extends TFolderResource implements AddressBookResource, ReportableResource {

    private static final Logger log = LoggerFactory.getLogger(TCalendarResource.class);

    public TAddressBookResource(TFolderResource parent, String name) {
        super(parent, name);
    }

    @Override
    protected Object clone(TFolderResource newParent, String newName) {
        return new TCalendarResource(newParent, newName);
    }

    @Override
    public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException {
        try {
            log.debug("createNew: " + contentType);
            //        if (contentType.startsWith("text/calendar")) {
            TContact e = new TContact(this, newName);
            e.replaceContent(inputStream, length);
            log.debug("created contact: " + e.name);
            return e;
            //        } else {
            //            throw new RuntimeException("eek");
            //            //log.debug( "creating a normal resource");
            //        }
            //        }
        } catch (BadRequestException ex) {
            throw new RuntimeException(ex);
        } catch (ConflictException ex) {
            throw new RuntimeException(ex);
        } catch (NotAuthorizedException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public InternationalizedString getDescription() {
        return new InternationalizedString("fr-CA", "Adresses de Oliver Daboo");
    }

    @Override
    public void setDescription(InternationalizedString description) {
    }

    @Override
    public Long getMaxResourceSize() {
        return 102400L;
    }

    @Override
    public AddressDataTypeList getSupportedAddressData() {
        AddressDataTypeList supportedAddresses = new AddressDataTypeList();
        supportedAddresses.add(new Pair<String, String>("text/vcard", "3.0"));
        return supportedAddresses;
    }
}
