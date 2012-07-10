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

import io.milton.mail.MailResourceFactory;
import io.milton.mail.Mailbox;
import io.milton.mail.MailboxAddress;


/**
 * This adds email support to the CalDAV demo application
 *
 * NOTE THAT MILTON CALDAV DOES NOT DEPEND ON THE GEROA PROJECT!
 *
 * This is just here because it can be convenient to test some caldav clients
 * with an integrated email and caldav server
 *
 *
 * @author brad
 */
public class TMailResourceFactory implements MailResourceFactory{

    private final TResourceFactory resourceFactory;

    public TMailResourceFactory(TResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }

    public Mailbox getMailbox(MailboxAddress add) {
        return resourceFactory.findUser(add.user);
    }

}
