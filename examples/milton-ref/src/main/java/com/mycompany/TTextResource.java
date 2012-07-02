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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import io.milton.http.FileItem;
import io.milton.http.Range;
import io.milton.property.BeanPropertyResource;
import io.milton.property.MultiNamespaceCustomPropertyResource;
import io.milton.property.PropertySource.PropertyAccessibility;
import io.milton.property.PropertySource.PropertyMetaData;
import io.milton.resource.PostableResource;
import io.milton.resource.ReplaceableResource;



/**
 * Demonstrates implementing:
 *   - MultiNamespaceCustomPropertyResource - for getting and setting custom properties
 *   - BeanPropertyResource - showing how to edit bean properties
 *   - ReplaceableResource - for replacing content on PUTs to existing resources
 *   - PostableResource - for form processing
 *
 */
// This annotation allows us to edit the text property
@BeanPropertyResource("http://milton.ettrema.com/demo/beanprop") 
public class TTextResource extends TResource implements PostableResource, ReplaceableResource, MultiNamespaceCustomPropertyResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TTextResource.class );
    private static final String START_CONTENT = "<textarea name=\"text\" cols=\"60\" rows=\"20\">";
    private static final String END_CONTENT = "</textarea>";
    private static final String NS_EXAMPLE = "http://milton.ettrema.com/demo/multins";
    private String text;
    private Map<String, String> props = new HashMap<String, String>();

    public TTextResource( TFolderResource parent, String name, String text ) {
        super( parent, name );
        this.text = text;
        props.put( "helloWorldProperty", "i am " + name );
    }

    @Override
    protected Object clone( TFolderResource newParent ) {
        return new TTextResource( newParent, name, text );
    }

    public String getContentType( String accept ) {
        return "text";
    }

    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException {
        PrintWriter printer = new PrintWriter( out, true );
        sendContentStart( printer );
        sendContentMiddle( printer );
        sendContentFinish( printer );
    }

    protected void sendContentMiddle( final PrintWriter printer ) {
        print( printer, "<form method='post' action='" + this.getHref() + "'>" );
        print( printer, "<fieldset>" );
        print( printer, "<input type='text' name='name' value='" + this.getName() + "'/>" );
        print( printer, "<br/>" );
        printer.print( START_CONTENT );
        print( printer, text );
        printer.print( END_CONTENT );
        print( printer, "<br/>" );
        printer.print( "<input type='submit' />" );
        print( printer, "</fieldset>" );
        print( printer, "</form>" );
    }

    protected void sendContentFinish( final PrintWriter printer ) {
        printer.print( "</body></html>" );
        printer.flush();
    }

    protected void sendContentStart( final PrintWriter printer ) {
        printer.print( "<html>" );
        printer.print( "<head>" );
        printer.print( "<title>page: " + this.getName() + "</title>" );
        printer.print( "</head>" );
        printer.print( "<body>" );
        printer.print( "<h1>" + getName() + "</h1>" );
        sendContentMenu( printer );
    }

    protected void sendContentMenu( final PrintWriter printer ) {
        printer.print( "<ul>" );
        for( TResource r : parent.children ) {
            printer.print( "<li><a href='" + r.getHref() + "'>" + r.getName() + "</a></li>" );
        }
        printer.print( "</ul>" );
    }

    public String processForm( Map<String, String> parameters, Map<String, FileItem> files ) {
        log.debug( "processForm: " + parameters.size() );
        for( String nm : parameters.keySet() ) {
            log.debug( " - param: " + nm );
        }
        String newName = parameters.get( "name" );
        if( newName != null ) {
            this.name = newName;
        }
        String newContent = parameters.get( "text" );
        this.text = newContent;
        this.modDate = new Date();
        return null;
    }

    public void replaceContent( InputStream in, Long length ) {
        try {
            String newContent = TFolderResource.readStream( in ).toString();
            int pos = newContent.indexOf( START_CONTENT );
            if( pos >= 0 ) {
                newContent = newContent.substring( pos + START_CONTENT.length() );
            }
            pos = newContent.indexOf( END_CONTENT );
            if( pos >= 0 ) {
                newContent = newContent.substring( 0, pos );
            }
            log.debug( "new content: " + newContent );
            this.text = newContent;
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }

    }

    public Object getProperty( QName name ) {
        if( name.getNamespaceURI().equals( NS_EXAMPLE ) ) {
            return props.get( name.getLocalPart() );
        } else {
            return PropertyMetaData.UNKNOWN;
        }
    }

    public void setProperty( QName name, Object value ) {
        if( name.getNamespaceURI().equals( NS_EXAMPLE ) ) {
            props.put( name.getLocalPart(), (String) value );
        }
    }

    public PropertyMetaData getPropertyMetaData( QName name ) {
        if( name.getNamespaceURI().equals( NS_EXAMPLE ) ) {
            return new PropertyMetaData( PropertyAccessibility.WRITABLE, String.class );
        } else {
            return PropertyMetaData.UNKNOWN;
        }
    }

    public List<QName> getAllPropertyNames() {
        List<QName> list = new ArrayList<QName>();
        for( String key : props.keySet() ) {
            list.add( new QName( NS_EXAMPLE, key ) );
        }
        return list;
    }

    public String getText() {
        return text;
    }

    public void setText( String text ) {
        this.text = text;
    }
    
}
