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

import com.bradmcevoy.http.Resource;
import com.ettrema.mail.MessageFolder;
import com.ettrema.mail.MessageResource;
import com.ettrema.mail.StandardMessageFactoryImpl;
import com.ettrema.mail.StandardMessageImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author brad
 */
public class TMailFolder extends TFolderResource implements MessageFolder {

    private int nextMsgId = 1000;

    public TMailFolder(TFolderResource parent, String name) {
        super(parent, name);
    }

    public Collection<MessageResource> getMessages() {
        List<MessageResource> list = new ArrayList<MessageResource>();
        for( Resource r : this.children ) {
            if( r instanceof MessageResource) {
                list.add((MessageResource) r);
            }
        }
        return list;
    }

    public int numMessages() {
        return getMessages().size();
    }

    public int totalSize() {
        int size = 0;
        for( Resource r : this.children ) {
            if( r instanceof MessageResource) {
                MessageResource mr = (MessageResource) r;
                size += mr.getSize();
            }
        }
        return size;

    }

    public void storeMail(MimeMessage mm) {
        StandardMessageFactoryImpl factoryImpl = new StandardMessageFactoryImpl();
        StandardMessageImpl sm = new StandardMessageImpl();
        factoryImpl.toStandardMessage(mm, sm);
        TMessageResource mr = new TMessageResource(this,"msg-" + nextMsgId++, sm);
        System.out.println("stored message in: " + mr.getName());
    }

}
