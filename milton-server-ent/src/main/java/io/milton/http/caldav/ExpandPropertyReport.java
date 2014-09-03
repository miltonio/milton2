/*
 * Copyright 2012 McEvoy Software Ltd.
 */

package io.milton.http.caldav;

import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.report.Report;
import io.milton.http.values.HrefList;
import io.milton.http.values.PropFindResponseList;
import io.milton.http.values.ValueAndType;
import io.milton.http.webdav.PropFindPropertyBuilder;
import io.milton.http.webdav.PropFindResponse;
import io.milton.http.webdav.PropFindXmlGenerator;
import io.milton.http.webdav.PropertiesRequest;
import io.milton.http.webdav.PropertiesRequest.Property;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.resource.PropFindableResource;
import io.milton.resource.Resource;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.namespace.QName;
import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http://tools.ietf.org/html/rfc3253#section-3.8
 * 
 * Many property values are defined as a DAV:href, or a set of DAV:href
elements.  The DAV:expand-property report provides a mechanism for
retrieving in one request the properties from the resources
identified by those DAV:href elements.  This report not only
decreases the number of requests required, but also allows the server
to minimize the number of separate read transactions required on the
underlying versioning store.

The DAV:expand-property report SHOULD be supported by all resources
that support the REPORT method.

Marshalling:

The request body MUST be a DAV:expand-property XML element.

<!ELEMENT expand-property (property*)>
<!ELEMENT property (property*)>
<!ATTLIST property name NMTOKEN #REQUIRED>
name value: a property element type
<!ATTLIST property namespace NMTOKEN "DAV:">
namespace value: an XML namespace

The response body for a successful request MUST be a
DAV:multistatus XML element.

multistatus: see RFC 2518, Section 12.9

The properties reported in the DAV:prop elements of the
DAV:multistatus element MUST be those identified by the
DAV:property elements in the DAV:expand-property element.  If
there are DAV:property elements nested within a DAV:property
element, then every DAV:href in the value of the corresponding
property is replaced by a DAV:response element whose DAV:prop
elements report the values of the properties identified by the
nested DAV:property elements.  The nested DAV:property elements
can in turn contain DAV:property elements, so that multiple levels
of DAV:href expansion can be requested.

Note that a validating parser MUST be aware that the DAV:expand-
property report effectively modifies the DTD of every property by
replacing every occurrence of "href" in the DTD with "href |
response".
 * 
 *  REPORT /foo.html HTTP/1.1
Host: www.webdav.org
Content-Type: text/xml; charset="utf-8"
Content-Length: xxxx

<?xml version="1.0" encoding="utf-8" ?>
<D:expand-property xmlns:D="DAV:">
<D:property name="version-history">
<D:property name="version-set">
<D:property name="creator-displayname"/>
<D:property name="activity-set"/>
</D:property>
</D:property>
</D:expand-property>

>>RESPONSE

HTTP/1.1 207 Multi-Status
Content-Type: text/xml; charset="utf-8"
Content-Length: xxxx

<?xml version="1.0" encoding="utf-8" ?>
<D:multistatus xmlns:D="DAV:">
<D:response>
<D:href>http://www.webdav.org/foo.html</D:href>
<D:propstat>
<D:prop>
<D:version-history>
<D:response>
<D:href>http://repo.webdav.org/his/23</D:href>
<D:propstat>
<D:prop>
<D:version-set>
<D:response>
<D:href>http://repo.webdav.org/his/23/ver/1</D:href>
<D:propstat>
<D:prop>
<D:creator-displayname>Fred</D:creator-displayname>
<D:activity-set> 
 <D:href>http://www.webdav.org/ws/dev/sally</D:href> 
 </D:activity-set> </D:prop>
<D:status>HTTP/1.1 200 OK</D:status>
</D:propstat> </D:response>
<D:response>
<D:href>http://repo.webdav.org/his/23/ver/2</D:href>
<D:propstat>
<D:prop>
<D:creator-displayname>Sally</D:creator-displayname>
<D:activity-set>
<D:href>http://repo.webdav.org/act/add-refresh-cmd</D:href>
</D:activity-set> </D:prop>
<D:status>HTTP/1.1 200 OK</D:status>
</D:propstat> </D:response>
</D:version-set> </D:prop>
<D:status>HTTP/1.1 200 OK</D:status>
</D:propstat> </D:response>
</D:version-history> </D:prop>
<D:status>HTTP/1.1 200 OK</D:status>
</D:propstat> </D:response>
</D:multistatus>

In this example, the DAV:creator-displayname and DAV:activity-set
properties of the versions in the DAV:version-set of the
DAV:version-history of http://www.webdav.org/foo.html are reported.
 *
 * @author bradm
 */
public class ExpandPropertyReport implements Report {

	private static final Logger log = LoggerFactory.getLogger(MultiGetReport.class);
	private final ResourceFactory resourceFactory;
	private final PropFindPropertyBuilder propertyBuilder;
	private final PropFindXmlGenerator xmlGenerator;

	public ExpandPropertyReport(ResourceFactory resourceFactory, PropFindPropertyBuilder propertyBuilder, PropFindXmlGenerator xmlGenerator) {
		this.resourceFactory = resourceFactory;
		this.propertyBuilder = propertyBuilder;
		this.xmlGenerator = xmlGenerator;
	}

	@Override
	public String process(String host, String path, Resource calendar, Document doc) throws NotAuthorizedException, BadRequestException {
		log.debug("process");

		PropertiesRequest parseResult = parse(doc.getRootElement());
		
		List<PropFindResponse> propFindResponses;
		try {
			PropFindableResource pfr = (PropFindableResource) calendar;
			propFindResponses = propertyBuilder.buildProperties(pfr, 1, parseResult, path);

			for (PropFindResponse r : propFindResponses) {
				Set<Entry<QName, ValueAndType>> set = r.getKnownProperties().entrySet();
				set = new HashSet<Entry<QName, ValueAndType>>(set);
				for (Entry<QName, ValueAndType> p : set) {
					Object val = p.getValue().getValue();
					QName name = p.getKey();
					if (val instanceof HrefList) {
						HrefList hrefList = (HrefList) val;
						Property prop = parseResult.get(name);
						PropFindResponseList propFindResponseList = toResponseList(host, hrefList, prop);
						replaceHrefs(host, propFindResponseList, prop);
						r.getKnownProperties().remove(name);
						r.getKnownProperties().put(name, new ValueAndType(propFindResponseList, PropFindResponseList.class));
					}
				}
			}
		} catch (URISyntaxException ex) {
			throw new RuntimeException("Exception parsing url, indicating the requested URL is not correctly encoded. Please check the client application.", ex);
		}

		//show("",propFindResponses);
		
		String xml = xmlGenerator.generate(propFindResponses);
		return xml;
	}

	public PropertiesRequest parse(Element elProp) {
		Set<Property> set = new HashSet<Property>();
		for (Object o : elProp.getChildren()) {
			if (o instanceof Element) {
				Element el = (Element) o;
				if (el.getName().equals("property")) {
					QName name = getQName(el);
					Set<Property> nested = parseChildren(el);
					Property p = new Property(name, nested);
					set.add(p);
				}
			}
		}
		PropertiesRequest pr = new PropertiesRequest(set);
		return pr;
	}

	private Set<Property> parseChildren(Element elProp) {
		Set<Property> set = new HashSet<Property>();
		for (Object o : elProp.getChildren()) {
			if (o instanceof Element) {
				Element el = (Element) o;
				if (el.getName().equals("property")) {
					QName name = getQName(el);					
					Set<Property> nested = parseChildren(el);
					Property p = new Property(name, nested);
					set.add(p);
				}
			}
		}
		return set;
	}

	private QName getQName(Element el) {
		String local = el.getAttributeValue("name");
		String ns = el.getAttributeValue("namespace");
		if (ns == null) {
			ns = WebDavProtocol.DAV_URI;
		}
		QName name = new QName(ns, local);
		return name;
	}

	@Override
	public String getName() {
		return "expand-property";
	}

	private PropFindResponseList toResponseList(String host, HrefList hrefList, Property prop) throws URISyntaxException, NotAuthorizedException, BadRequestException {
		PropFindResponseList list = new PropFindResponseList();
		for (String href : hrefList) {
			Resource r = resourceFactory.getResource(host, href);
			if (r != null) {
				if (r instanceof PropFindableResource) {
					PropFindableResource pfr = (PropFindableResource) r;
					PropertiesRequest propertyRequest = new PropertiesRequest(prop.getNested());
					List<PropFindResponse> propFindResponses = propertyBuilder.buildProperties(pfr, 0, propertyRequest, href);
					// should be only one
					list.addAll(propFindResponses);
				}
			}
		}
		return list;
	}

	private void replaceHrefs(String host, PropFindResponseList propFindResponseList, Property prop) throws URISyntaxException, NotAuthorizedException, BadRequestException {		
		for (PropFindResponse r : propFindResponseList) {
			Set<Entry<QName, ValueAndType>> set = r.getKnownProperties().entrySet();
			set = new HashSet<Entry<QName, ValueAndType>>(set);
			for (Entry<QName, ValueAndType> p : set) {
				Object val = p.getValue().getValue();
				QName name = p.getKey();
				if (val instanceof HrefList) {
					HrefList hrefList = (HrefList) val;
					Property nestedProp = prop.getNestedMap().get(name);
					PropFindResponseList nestedList = toResponseList(host, hrefList, nestedProp);
					replaceHrefs(host, nestedList, nestedProp);
					r.getKnownProperties().remove(name);
					r.getKnownProperties().put(name, new ValueAndType(nestedList, PropFindResponseList.class));
				}
			}
		}
	}

	private void show(String prefix, List<PropFindResponse> propFindResponses) {
		for( PropFindResponse p : propFindResponses ) {
			for( Entry<QName, ValueAndType> e : p.getKnownProperties().entrySet()) {
				Object o = e.getValue().getValue();
				if( o instanceof PropFindResponseList) {
					PropFindResponseList childList = (PropFindResponseList) o;
					show(prefix + "   ", childList);
				} else {
					
				}
			}
		}
	}
}
