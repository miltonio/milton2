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

import io.milton.mail.MessageFolder;
import io.milton.mail.MessageResource;
import io.milton.mail.StandardMessageFactoryImpl;
import io.milton.mail.StandardMessageImpl;
import io.milton.resource.Resource;
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

    @Override
    public Collection<MessageResource> getMessages() {
        List<MessageResource> list = new ArrayList<MessageResource>();
        for( Resource r : this.children ) {
            if( r instanceof MessageResource) {
                list.add((MessageResource) r);
            }
        }
        return list;
    }

    @Override
    public int numMessages() {
        return getMessages().size();
    }

    @Override
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
