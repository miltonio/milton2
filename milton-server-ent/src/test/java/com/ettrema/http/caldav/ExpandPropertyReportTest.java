/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * 
 */

package com.ettrema.http.caldav;

import io.milton.http.ResourceFactory;
import io.milton.http.caldav.ExpandPropertyReport;
import io.milton.http.values.*;
import io.milton.http.webdav.DefaultPropFindPropertyBuilder;
import io.milton.http.webdav.PropFindPropertyBuilder;
import io.milton.http.webdav.PropFindXmlGenerator;
import io.milton.http.webdav.PropFindXmlGeneratorHelper;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.property.PropertySource;
import io.milton.resource.PropFindableResource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import javax.xml.namespace.QName;
import junit.framework.TestCase;

import org.jdom.Document;
import static org.easymock.EasyMock.*;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author bradm
 */
public class ExpandPropertyReportTest extends TestCase {
	
	ResourceFactory resourceFactory;
	PropFindableResource otherResource;
        PropFindableResource otherResource2;
	PropFindPropertyBuilder propertyBuilder;
	ExpandPropertyReport rep; 
	PropertySource propertySource;
	PropertySource.PropertyMetaData meta1;
        PropertySource.PropertyMetaData meta2;
	List<PropertySource> propertySources;
	PropFindXmlGenerator xmlGenerator;
	PropFindXmlGeneratorHelper xmlGeneratorHelper;
	ValueWriters valueWriters;
	
	public ExpandPropertyReportTest(String testName) {
		super(testName);
	}
	
	@Override
	protected void setUp() throws Exception {
		propertySource = createMock(PropertySource.class);
		xmlGeneratorHelper = new PropFindXmlGeneratorHelper();
		List<ValueWriter> writers = Arrays.asList(new HrefListValueWriter(), new PropFindResponseListWriter(xmlGeneratorHelper), new ToStringValueWriter());
		valueWriters = new ValueWriters(writers);
		xmlGeneratorHelper.setValueWriters(valueWriters);
		xmlGenerator = new PropFindXmlGenerator(valueWriters);
		propertySources = Arrays.asList(propertySource);
		meta1 = new PropertySource.PropertyMetaData(PropertySource.PropertyAccessibility.READ_ONLY, HrefList.class);
                meta2 = new PropertySource.PropertyMetaData(PropertySource.PropertyAccessibility.READ_ONLY, HrefList.class);
		propertyBuilder = new DefaultPropFindPropertyBuilder(propertySources);
		otherResource = createMock(PropFindableResource.class);
                otherResource2 = createMock(PropFindableResource.class);
		resourceFactory = createMock(ResourceFactory.class);
				
		rep = new ExpandPropertyReport(resourceFactory, propertyBuilder, xmlGenerator);
		
	}

	public void testProcess() throws Exception {
		PropFindableResource pfr = createMock(PropFindableResource.class);
		SAXBuilder builder = new org.jdom.input.SAXBuilder();
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
						"<D:expand-property xmlns:D=\"DAV:\">" +
						"<D:property name=\"version-history\">" +
						" <D:property name=\"version-set\">" +
						"   <D:property name=\"creator-displayname\"/>" +
						"   <D:property name=\"activity-set\"/>" +
						" </D:property>" +
					   "</D:property>" +
					 "</D:expand-property>";
		
		InputStream in = new ByteArrayInputStream(xml.getBytes());
		Document doc = builder.build(in);
		
		HrefList hrefList1 = new HrefList();
		hrefList1.add("/versionHistory");
		HrefList hrefList2 = new HrefList();
		hrefList2.add("/versionSet");
                
		expect(propertySource.getPropertyMetaData(eq(new QName(WebDavProtocol.DAV_URI, "version-history")), same(pfr))).andReturn(meta1);
                expect(propertySource.getPropertyMetaData(eq(new QName(WebDavProtocol.DAV_URI, "version-set")), same(otherResource))).andReturn(meta2);
                expect(propertySource.getPropertyMetaData(eq(new QName(WebDavProtocol.DAV_URI, "creator-displayname")), same(otherResource2))).andReturn(meta2);
                expect(propertySource.getPropertyMetaData(eq(new QName(WebDavProtocol.DAV_URI, "activity-set")), same(otherResource2))).andReturn(meta2);
                
		expect(propertySource.getProperty(eq(new QName(WebDavProtocol.DAV_URI, "version-history")), same(pfr))).andReturn(hrefList1);
                expect(propertySource.getProperty(eq(new QName(WebDavProtocol.DAV_URI, "version-set")), same(otherResource))).andReturn(hrefList2);
                expect(propertySource.getProperty(eq(new QName(WebDavProtocol.DAV_URI, "creator-displayname")), same(otherResource2))).andReturn("Joe");
                expect(propertySource.getProperty(eq(new QName(WebDavProtocol.DAV_URI, "activity-set")), same(otherResource2))).andReturn("Activity1");
                
		expect(resourceFactory.getResource("host", "/versionHistory")).andReturn(otherResource);
                expect(resourceFactory.getResource("host", "/versionSet")).andReturn(otherResource2);
		replay(propertySource,  resourceFactory);
		
		xml = rep.process("host", "/path",  pfr, doc);
		
		System.out.println("expand property report:");
		System.out.println(xml);
		verify(propertySource,  resourceFactory);
	}
//	
//	public void testParse() throws JDOMException, IOException, NotAuthorizedException {
//		SAXBuilder builder = new org.jdom.input.SAXBuilder();
//		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
//						"<D:expand-property xmlns:D=\"DAV:\">" +
//						"<D:property name=\"version-history\">" +
//						" <D:property name=\"version-set\">" +
//						"   <D:property name=\"creator-displayname\"/>" +
//						"   <D:property name=\"activity-set\"/>" +
//						" </D:property>" +
//					   "</D:property>" +
//					 "</D:expand-property>";
//		
//		InputStream in = new ByteArrayInputStream(xml.getBytes());
//		Document doc = builder.build(in);
//		PropertiesRequest req = rep.parse(doc.getRootElement());
//
//		assertEquals(1, req.getNames().size());
//		Property prop1 = req.getProperties().iterator().next();
//		assertEquals("version-history", prop1.getName().getLocalPart());
//		assertEquals(1, prop1.getNestedMap().size());
//		Property prop2 = prop1.getNested().iterator().next();
//		assertEquals("version-set", prop2.getName().getLocalPart());
//		System.out.println("assert: version-set children: " + prop2.getNestedMap().size());
//		assertEquals(2, prop2.getNestedMap().size());
//	}
	

	public void testGetName() {
	}
}
