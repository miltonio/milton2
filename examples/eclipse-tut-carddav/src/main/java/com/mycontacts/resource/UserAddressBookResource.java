/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mycontacts.resource;

import com.mycontacts.ChildUtils;
import com.mycontacts.model.ContactsDao;
import com.mycontacts.model.ContactsDao.Contact;
import io.milton.common.InternationalizedString;
import io.milton.http.Auth;
import io.milton.http.Range;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.http.values.AddressDataTypeList;
import io.milton.http.values.Pair;
import io.milton.resource.AddressBookResource;
import io.milton.resource.GetableResource;
import io.milton.resource.PutableResource;
import io.milton.resource.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author brad
 */
public class UserAddressBookResource extends AbstractResource implements AddressBookResource, PutableResource, GetableResource {

    private ArrayList<Resource> children;

    public UserAddressBookResource(UserResource parent) {
        super(parent, parent.contactManager);
    }

    @Override
    public String getName() {
        return "abs";
    }

    @Override
    public Resource createNew(String newName, InputStream in, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOUtils.copy(in, bout);
        String icalData = bout.toString("UTF-8");
        Contact cNew = contactManager.createContact(newName, icalData);
        return new ContactResource(this, cNew);
    }

    @Override
    public InternationalizedString getDescription() {
        return new InternationalizedString(null, "Common addressbook for all users");
    }

    @Override
    public void setDescription(InternationalizedString description) {
        // not supported
    }

    @Override
    public List<Pair<String, String>> getSupportedAddressData() {
        AddressDataTypeList supportedAddresses = new AddressDataTypeList();
        supportedAddresses.add(new Pair<String, String>("text/vcard", "3.0"));
        return supportedAddresses;
    }

    @Override
    public Long getMaxResourceSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public String getCTag() {
        return "v" + contactManager.getContactsDao().getContactsVersion();
    }

    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        return ChildUtils.child(childName, getChildren());
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        if (children == null) {
            children = new ArrayList<Resource>();
            for (ContactsDao.Contact g : contactManager.getContactsDao().getContacts()) {
                children.add(new ContactResource(this, g));
            }
        }
        return children;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        PrintWriter pw = new PrintWriter(out);
        pw.println("<html>");
        pw.println("<body>");
        pw.println("<table border='1' cellspacing='3'>");
        pw.println("<tr>");
        pw.println("<th>File name</th>");
        pw.println("<th>UID</th>");
        pw.println("<th>Givenname</th>");
        pw.println("<th>Surname</th>");
        pw.println("<th>Email</th>");
        pw.println("<th>Phone</th>");
        pw.println("</tr>");
        for( Contact c : contactManager.getContactsDao().getContacts() ) {
            pw.println("<tr>");
            pw.println("<td>" + c.getFileName() + "</td>");
            pw.println("<td>" + c.getUid() + "</td>");
            pw.println("<td>" + c.getGivenName() + "</td>");
            pw.println("<td>" + c.getSurName() + "</td>");
            pw.println("<td>" + c.getMail() + "</td>");
            pw.println("<td>" + c.getTelephonenumber() + "</td>");
            pw.println("</tr>");
        }
        pw.println("</table>");
        
        pw.println("</body>");
        pw.println("</html>");
        pw.flush();
        pw.close();
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        return "text/html";
    }

    @Override
    public Long getContentLength() {
        return null;
    }
}
