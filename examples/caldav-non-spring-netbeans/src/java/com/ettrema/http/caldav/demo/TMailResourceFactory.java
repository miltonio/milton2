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

package com.ettrema.http.caldav.demo;

import com.ettrema.mail.MailResourceFactory;
import com.ettrema.mail.Mailbox;
import com.ettrema.mail.MailboxAddress;

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
