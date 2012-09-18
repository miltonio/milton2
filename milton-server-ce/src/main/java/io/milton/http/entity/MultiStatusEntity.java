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

package io.milton.http.entity;

import io.milton.http.HrefStatus;
import io.milton.http.Response;
import io.milton.http.XmlWriter;
import io.milton.http.webdav.WebDavProtocol;

import java.io.OutputStream;
import java.util.List;

public class MultiStatusEntity implements Response.Entity {

    private List<HrefStatus> statii;

    public MultiStatusEntity(List<HrefStatus> statii) {
        this.statii = statii;
    }

    public List<HrefStatus> getStatii() {
        return statii;
    }

    @Override
    public void write(Response response, OutputStream outputStream) throws Exception {

        XmlWriter writer = new XmlWriter( response.getOutputStream() );
        writer.writeXMLHeader();
        writer.open( "multistatus xmlns:D" + "=\"" + WebDavProtocol.NS_DAV + ":\"" ); // only single namespace for this method
        writer.newLine();
        for( HrefStatus status : getStatii()) {
            XmlWriter.Element elResponse = writer.begin( "response" ).open();
            writer.writeProperty( "", "href", status.href );
            writer.writeProperty( "", "status", status.status.code + "" );
            elResponse.close();
        }
        writer.close( "multistatus" );
        writer.flush();
    }
}
