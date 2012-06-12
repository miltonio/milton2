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

package io.milton.httpclient;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class ReportMethod extends HttpEntityEnclosingRequestBase {

    private static final Logger log = LoggerFactory.getLogger( PropFindMethod.class );

    public ReportMethod( String uri ) throws URISyntaxException {
        setURI(new URI(uri));
    }

    @Override
    public String getMethod() {
        return "REPORT";
    }

    public Document getResponseAsDocument(HttpClient client) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Utils.executeHttpWithStatus(client, this, out);
        String xml = out.toString();
        try {
            Document document = RespUtils.getJDomDocument( new ByteArrayInputStream( xml.getBytes() ) );
            return document;
        } catch( JDOMException ex ) {
            throw new RuntimeException(xml, ex );
        }
    }
}
