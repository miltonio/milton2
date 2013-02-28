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

package io.milton.mail;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class MailboxAddress implements Serializable{

    private static final long serialVersionUID = 1L;

    static String removeSurroundingDelimiters(String p, String delim1, String delim2) {
        int pos = p.indexOf(delim1);
        if (pos >= 0) {
            p = p.substring(pos + 1);
        }
        pos = p.indexOf(delim2);
        if (pos >= 0) {
            p = p.substring(0, pos);
        }
        return p;
    }

    public static MailboxAddress parse(String address) throws IllegalArgumentException {
        if( address == null  ) throw new IllegalArgumentException("address argument is null");
        if( address.length() == 0 ) throw new IllegalArgumentException("address argument is empty");

        int posOpenBracket = address.indexOf("<");
        if( posOpenBracket > 0 ) {
            String p = address.substring(0, posOpenBracket-1);
            p = removeSurroundingDelimiters(p, "\"", "\"");

            String add = address.substring(posOpenBracket+1);
            add = removeSurroundingDelimiters(add, "<", ">");
            String[] arr = add.split("[@]");
            if( arr.length != 2 ) throw new IllegalArgumentException("Not a valid email address: " + address);
            return new MailboxAddress(arr[0], arr[1],p);
        } else {
            String[] arr = address.split("[@]");
            if( arr.length != 2 ) throw new IllegalArgumentException("Not a valid email address: " + address);
            return new MailboxAddress(arr[0], arr[1]);
        }
    }

    public final String user;
    public final String domain;
    public final String personal;





    public MailboxAddress(String user, String domain, String personal) {
        this.user = user;
        this.domain = domain;
        this.personal = personal;
    }


    public MailboxAddress(String user, String domain) {
        this.user = user;
        this.domain = domain;
        this.personal = null;
    }

    @Override
    public String toString() {
        if( personal == null ) {
            return toPlainAddress();
        } else {
            return "\"" + personal + "\"" + " <" + toPlainAddress() + ">";
        }
    }

    public String toPlainAddress() {
        return user + "@" + domain;
    }

    public InternetAddress toInternetAddress() {
        try {
            if( personal == null ) {
                return  new InternetAddress(user + "@" + domain);
            } else {
                try {
                    return new InternetAddress(user + "@" + domain, personal);
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } catch (AddressException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getDomain() {
        return domain;
    }

    public String getPersonal() {
        return personal;
    }

    public String getUser() {
        return user;
    }

    /**
     * Returns a representative name for this address. This is the personal
     * portion if present, otherwise it is the user portion.
     * 
     * @return
     */
    public String getDisplayName() {
        if( personal != null && personal.length() > 0 ) {
            return personal;
        } else {
            return user;
        }
    }
    
    
}
