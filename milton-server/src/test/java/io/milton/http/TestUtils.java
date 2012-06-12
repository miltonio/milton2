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

package io.milton.http;

import io.milton.common.Utils;
import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;
import io.milton.common.Path;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import junit.framework.TestCase;

public class TestUtils extends TestCase {

    public void test() throws NotAuthorizedException, BadRequestException {
        ColRes col1 = new ColRes("col1");
        ColRes col2 = new ColRes("col2");
        col1.children.put("col2",col2);
        Res page = new Res("page");
        col2.children.put("page",page);
        
        Path path = Path.path("col2/page");
        Resource r = Utils.findChild(col1, path);
        assertEquals(page, r);
    }

    public void testGetProtocol() {
        String url = "http://abc.com/aaa";
        assertEquals("http", Utils.getProtocol(url));

        url = "http://abc.com:80/aaa";
        assertEquals("http", Utils.getProtocol(url));

        url = "https://abc.com/aaa";
        assertEquals("https", Utils.getProtocol(url));

    }

    class Res implements Resource {

        final String name;
        final UUID id;

        public Res(String name) {
            this.name = name;
            this.id = UUID.randomUUID();
        }

        
        public String getUniqueId() {
            return id.toString();
        }
        
        
                       
        
        public String getName() {
            return name;
        }

        
        public Object authenticate(String user, String password) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
        public boolean authorise(Request request, Method method, Auth auth) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
        public String getRealm() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
        public Date getModifiedDate() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
        public Long getContentLength() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
        public String getContentType(String accepts) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
        public String checkRedirect(Request request) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
        public int compareTo(Resource o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }        
    }
    
    class ColRes extends Res implements CollectionResource {

        public Map<String,Resource> children  = new HashMap<String,Resource>();

        public ColRes(String name) {
            super(name);
        }
        
        
        
        
        public List<? extends Resource> getChildren() {
            return new ArrayList<Resource>(children.values());
        }

        
        public Resource child(String childName) {
            return children.get(childName);
        }                        
    }
}
