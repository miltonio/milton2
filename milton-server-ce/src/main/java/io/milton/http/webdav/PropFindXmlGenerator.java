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

package io.milton.http.webdav;

import io.milton.http.HttpManager;
import io.milton.http.XmlWriter;
import io.milton.http.values.ValueWriters;
import org.apache.commons.io.output.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class PropFindXmlGenerator {

    private static final Logger log = LoggerFactory.getLogger( PropFindXmlGenerator.class );
    private final PropFindXmlGeneratorHelper helper;    

    public PropFindXmlGenerator( ValueWriters valueWriters ) {
        helper = new PropFindXmlGeneratorHelper(valueWriters);
    }

    PropFindXmlGenerator( PropFindXmlGeneratorHelper helper ) {
        this.helper = helper;
    }

    public String generate( List<PropFindResponse> propFindResponses ) {
        ByteArrayOutputStream responseOutput = new ByteArrayOutputStream();
        Map<String, String> mapOfNamespaces = helper.findNameSpaces( propFindResponses );
        ByteArrayOutputStream generatedXml = new ByteArrayOutputStream();
        XmlWriter writer = new XmlWriter( generatedXml );
        writer.writeXMLHeader();
        writer.open(WebDavProtocol.NS_DAV.getPrefix() ,"multistatus" + helper.generateNamespaceDeclarations( mapOfNamespaces ) );
        writer.newLine();
        helper.appendResponses( writer, propFindResponses, mapOfNamespaces );
        writer.close(WebDavProtocol.NS_DAV.getPrefix(),"multistatus" );
        writer.flush();
		if(log.isTraceEnabled()) {
			log.trace("---- PROPFIND response START: " + HttpManager.request().getAbsolutePath() + " -----");
			log.trace( generatedXml.toString() );
			log.trace("---- PROPFIND response END -----");
		}
        helper.write( generatedXml, responseOutput );
        try {
            return responseOutput.toString( "UTF-8" );
        } catch( UnsupportedEncodingException ex ) {
            throw new RuntimeException( ex );
        }
    }
}
