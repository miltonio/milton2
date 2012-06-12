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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 *
 * @author mcevoyb
 */
public class LockMethod extends HttpEntityEnclosingRequestBase {

    public LockMethod(String uri) throws URISyntaxException {
        setURI(new URI(uri));
    }

    @Override
    public String getMethod() {
        return "LOCK";
    }

    public String getLockToken(HttpResponse response) {
        try {
            Document document = getResponseAsDocument(response);
            if (document == null) {
                throw new RuntimeException("Got empty response to LOCK request");
            }
            Element root = document.getRootElement();
            List<Element> lockTokenEls = RespUtils.getElements(root, "locktoken");
            for (Element el : lockTokenEls) {
                String token = RespUtils.asString(el, "href");
                if (token == null) {
                    throw new RuntimeException("No href element in locktoken");
                }
                return token;
            }
            throw new RuntimeException("Didnt find a locktoken/href element in LOCK response");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Document getResponseAsDocument(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return null;
        }
        InputStream in = null;
        try {
            in = entity.getContent();
//        IOUtils.copy( in, out );
//        String xml = out.toString();
            Document document = RespUtils.getJDomDocument(in);
            return document;
        } catch (JDOMException ex) {
            throw new RuntimeException(ex);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
