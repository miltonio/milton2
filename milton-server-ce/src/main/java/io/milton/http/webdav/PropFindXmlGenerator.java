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

package io.milton.http.webdav;

import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.XmlWriter;
import io.milton.http.values.ValueWriters;
import java.io.OutputStream;
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

    /**
     * @param footerGenerator  Use this parameter for writing additional footer elements.
     */
    public void generate( List<PropFindResponse> propFindResponses, OutputStream responseOutput, boolean writeErrorProps, PropFindXmlFooter footerGenerator ) {
        Map<String, String> mapOfNamespaces = helper.findNameSpaces( propFindResponses );
        XmlWriter writer = new XmlWriter( responseOutput );
        writer.writeXMLHeader();
        writer.open(WebDavProtocol.NS_DAV.getPrefix() ,"multistatus" + helper.generateNamespaceDeclarations( mapOfNamespaces ) );
        writer.newLine();
        helper.appendResponses( writer, propFindResponses, mapOfNamespaces, writeErrorProps );
        if (footerGenerator != null) {
            footerGenerator.footer(writer);
        }
        writer.close(WebDavProtocol.NS_DAV.getPrefix(),"multistatus" );
        writer.flush();
    }

    public void generate( List<PropFindResponse> propFindResponses, OutputStream responseOutput, boolean writeErrorProps ) {
        generate(propFindResponses, responseOutput, writeErrorProps, null);
    }
	
    public String generate( List<PropFindResponse> propFindResponses ) {
        return generate(propFindResponses, null);
    }

    public String generate( List<PropFindResponse> propFindResponses, PropFindXmlFooter footerGenerator ) {
        boolean writeErrorProps = true;
        Request req = HttpManager.request();
        if( req != null ) {
            writeErrorProps = isBriefHeader(req);
        }
		
        ByteArrayOutputStream responseOutput = new ByteArrayOutputStream();
		
        generate(propFindResponses, responseOutput, writeErrorProps, footerGenerator);
		
        Map<String, String> mapOfNamespaces = helper.findNameSpaces( propFindResponses );
        ByteArrayOutputStream generatedXml = new ByteArrayOutputStream();
        XmlWriter writer = new XmlWriter( generatedXml );
        writer.writeXMLHeader();
        writer.open(WebDavProtocol.NS_DAV.getPrefix() ,"multistatus" + helper.generateNamespaceDeclarations( mapOfNamespaces ) );
        writer.newLine();
        helper.appendResponses( writer, propFindResponses, mapOfNamespaces, writeErrorProps );
        if (footerGenerator != null) {
            footerGenerator.footer(writer);
        }
        writer.close(WebDavProtocol.NS_DAV.getPrefix(),"multistatus" );
        writer.flush();
        if(log.isTraceEnabled()) {
            log.trace("---- PROPFIND response START: " + HttpManager.request().getAbsolutePath() + " -----");
            log.trace( generatedXml.toString() );
            log.trace("---- PROPFIND response END -----");
        }
		
        try {
            return responseOutput.toString("UTF-8");
        } catch( UnsupportedEncodingException ex ) {
            throw new RuntimeException( ex );
        }
    }
	
    private boolean isBriefHeader(Request request) {
        String b = request.getHeaders().get("Brief");
        return "t".equals(b);
    }
}
