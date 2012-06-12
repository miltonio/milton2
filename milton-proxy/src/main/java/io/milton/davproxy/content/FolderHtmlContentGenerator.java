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

package io.milton.davproxy.content;

import io.milton.http.HttpManager;
import io.milton.http.XmlWriter;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class FolderHtmlContentGenerator {
    
    private static final Logger log = LoggerFactory.getLogger(FolderHtmlContentGenerator.class);
    
    private String ssoPrefix; // currently not used - BM
    
    public void generateContent(CollectionResource folder, OutputStream out, String uri) throws NotAuthorizedException, BadRequestException {
        XmlWriter w = new XmlWriter(out);
        w.open("html");
        w.open("head");
        w.writeText(""
                + "<script type=\"text/javascript\" language=\"javascript1.1\">\n"
                + "    var fNewDoc = false;\n"
                + "  </script>\n"
                + "  <script LANGUAGE=\"VBSCRIPT\">\n"
                + "    On Error Resume Next\n"
                + "    Set EditDocumentButton = CreateObject(\"SharePoint.OpenDocuments.3\")\n"
                + "    fNewDoc = IsObject(EditDocumentButton)\n"
                + "  </script>\n"
                + "  <script type=\"text/javascript\" language=\"javascript1.1\">\n"
                + "    var L_EditDocumentError_Text = \"The edit feature requires a SharePoint-compatible application and Microsoft Internet Explorer 4.0 or greater.\";\n"
                + "    var L_EditDocumentRuntimeError_Text = \"Sorry, couldnt open the document.\";\n"
                + "    function editDocument(strDocument) {\n"
                + "      if (fNewDoc) {\n"
                + "        if (!EditDocumentButton.EditDocument(strDocument)) {\n"
                + "          alert(L_EditDocumentRuntimeError_Text); \n"
                + "        }\n"
                + "      } else { \n"
                + "        alert(L_EditDocumentError_Text); \n"
                + "      }\n"
                + "    }\n"
                + "  </script>\n");



        w.close("head");
        w.open("body");
        w.begin("h1").open().writeText(folder.getName()).close();
        w.open("table");
        for (Resource r : folder.getChildren()) {
            w.open("tr");

            w.open("td");
            String path = buildHref(uri, r.getName());
            w.begin("a").writeAtt("href", path).open().writeText(r.getName()).close();

            //w.begin("a").writeAtt("href", "#").writeAtt("onclick", "editDocument('" + path + "')").open().writeText("(edit with office)").close();

            w.close("td");

            w.begin("td").open().writeText(r.getModifiedDate() + "").close();
            w.close("tr");
        }
        w.close("table");
        w.close("body");
        w.close("html");
        w.flush();        
    }
    

    private String buildHref(String uri, String name) {
        // hmm, we're ignoring the path passed in uri. Dodgy...
        String abUrl = HttpManager.request().getAbsoluteUrl();
        if (!abUrl.endsWith("/")) {
            abUrl += "/";
        }
        if (ssoPrefix == null) {
            return abUrl + name;
        } else {
            // This is to match up with the prefix set on SimpleSSOSessionProvider in MyCompanyDavServlet
            String s = insertSsoPrefix(abUrl, ssoPrefix);
            return s += name;
        }
    }

    public static String insertSsoPrefix(String abUrl, String prefix) {
        // need to insert the ssoPrefix immediately after the host and port
        int pos = abUrl.indexOf("/", 8);
        String s = abUrl.substring(0, pos) + "/" + prefix;
        s += abUrl.substring(pos);
        return s;
    }
    
}
