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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 * @author mcevoyb
 */
public class RespUtils {

    private static final Logger log = LoggerFactory.getLogger( RespUtils.class );
    
    public static Namespace NS_DAV = Namespace.getNamespace("D", "DAV:");
    
    public static QName davName(String localName) {
        return new QName(NS_DAV.getURI(), localName, NS_DAV.getPrefix());
    }    
    
    
    public static String asString( Element el, String name ) {
        Element elChild = el.getChild( name, NS_DAV  );
        if( elChild == null ) {
            //log.debug("No child: " + name + " of " + el.getName());            
            return null;
        }
        return elChild.getText();
    }

    public static String asString( Element el, String name, Namespace ns ) {
//        System.out.println("asString: " + qname + " in: " + el.getName());
//        for( Object o : el.elements() ) {
//            Element e = (Element) o;
//            System.out.println(" - " + e.getQualifiedName());
//        }
        Element elChild = el.getChild( name, ns );
        if( elChild == null ) return null;
        return elChild.getText();
    }    
    
    public static Long asLong( Element el, String name ) {
        String s = asString( el, name );
        if( s == null || s.length()==0 ) return null;
        long l = Long.parseLong( s );
        return l;
    }
    
    public static Long asLong( Element el, String name, Namespace ns ) {
        String s = asString( el, name, ns );
        if( s == null || s.length()==0 ) return null;
        long l = Long.parseLong( s );
        return l;
    }    

    public static boolean hasChild( Element el, String name ) {
        if( el == null ) return false;
        List<Element> list = getElements(el, name);
        
        return !list.isEmpty();
    }    
    

    public static  List<Element> getElements(Element root, String name) {
        List<Element> list = new ArrayList<Element>();
        Iterator it = root.getDescendants(new ElementFilter(name));
        while(it.hasNext()) {
            Object o = it.next();
            if( o instanceof Element) {
                list.add((Element)o);
            }
        }
        return list;
    }    
    
    public static  org.jdom.Document getJDomDocument(InputStream in) throws JDOMException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			IOUtils.copy(in, bout);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}		
//		System.out.println("");
//		System.out.println(bout.toString());
//		System.out.println("");
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        try {
            SAXBuilder builder = new SAXBuilder();
            builder.setExpandEntities(false);
            return builder.build(bin);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }        
}
