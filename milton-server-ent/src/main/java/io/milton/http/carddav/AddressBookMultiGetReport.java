/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */

package io.milton.http.carddav;

import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.webdav.PropFindPropertyBuilder;
import io.milton.http.webdav.PropFindResponse;
import io.milton.http.webdav.PropFindXmlGenerator;
import io.milton.http.webdav.PropertiesRequest;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.http.report.Report;
import io.milton.resource.PropFindableResource;
import io.milton.resource.Resource;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The CARDDAV:addressbook-multiget REPORT is used to retrieve specific
 * address object resources from within a collection, if the Request-URI
 * is a collection, or to retrieve a specific address object resource,
 * if the Request-URI is an address object resource. This report is
 * similar to the CARDDAV:addressbook-query REPORT (see Section 8.6),
 * except that it takes a list of DAV:href elements instead of a
 * CARDDAV:filter element to determine which address object resources to
 * return.
 * 
 * >> Request <<
 * REPORT /home/bernard/addressbook/ HTTP/1.1
 * Host: addressbook.example.com
 * Depth: 1
 * Content-Type: text/xml; charset="utf-8"
 * Content-Length: xxxx
 * <?xml version="1.0" encoding="utf-8" ?>
 *      <C:addressbook-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:carddav">
 *          <D:prop>
 *              <D:getetag/>
 *              <C:address-data>
 *                  <C:prop name="VERSION"/>
 *                  <C:prop name="UID"/>
 *                  <C:prop name="NICKNAME"/>
 *                  <C:prop name="EMAIL"/>
 *                  <C:prop name="FN"/>
 *              </C:address-data>
 *          </D:prop>
 *          <D:href>/home/bernard/addressbook/vcf102.vcf</D:href>
 *          <D:href>/home/bernard/addressbook/vcf1.vcf</D:href>
 *      </C:addressbook-multiget>
 * 
 * 
 * @author nabil.shams
 */
public class AddressBookMultiGetReport implements Report {
    // todo- requires refactoring, as MultiGetReport also have same piece of code. 
    // todo- we should move the code to some common place for caldav and cardav. 
    // todo- maybe, we can have abstract class for both protocols, or we can add 
    // todo- util methods. 

    private static final Logger log = LoggerFactory.getLogger(AddressBookMultiGetReport.class);
    private final ResourceFactory resourceFactory;
    private final PropFindPropertyBuilder propertyBuilder;
    private final PropFindXmlGenerator xmlGenerator;
    private final Namespace NS_DAV = Namespace.getNamespace(WebDavProtocol.NS_DAV.getPrefix(), WebDavProtocol.NS_DAV.getName());

    public AddressBookMultiGetReport(ResourceFactory resourceFactory, PropFindPropertyBuilder propertyBuilder, PropFindXmlGenerator xmlGenerator) {
        this.resourceFactory = resourceFactory;
        this.propertyBuilder = propertyBuilder;
        this.xmlGenerator = xmlGenerator;
    }

    @Override
    public String getName() {
        return "addressbook-multiget";
    }

    @Override
    public String process(String host, String path, Resource calendar, Document doc) throws NotAuthorizedException, BadRequestException {
        log.debug("process");
        // The requested properties
        Set<QName> props = getProps(doc);
        // The requested resources
        List<String> hrefs = getHrefs(doc);

        PropertiesRequest parseResult = PropertiesRequest.toProperties(props);

        // Generate the response
        List<PropFindResponse> respProps = new ArrayList<PropFindResponse>();

        for (String href : hrefs) {
            Resource r = resourceFactory.getResource(host, href);
            if (r != null) {
                if (r instanceof PropFindableResource) {
                    PropFindableResource pfr = (PropFindableResource) r;
                    try {
                        respProps.addAll(propertyBuilder.buildProperties(pfr, 0, parseResult, href));
                    } catch (URISyntaxException ex) {
                        throw new RuntimeException("There was an unencoded url requested: " + href, ex);
                    }
                } else {
                    // todo
                }
            } else {
                // todo
            }
        }

        String xml = xmlGenerator.generate(respProps);
        return xml;
    }

    private List<String> getHrefs(Document doc) {
        List<String> list = new ArrayList<String>();
        for (Object o : doc.getRootElement().getChildren()) {
            if (o instanceof Element) {
                Element el = (Element) o;
                if (el.getName().equals("href")) {
                    list.add(el.getText());
                }
            }
        }
        return list;
    }

    private Set<QName> getProps(Document doc) {
        Element elProp = doc.getRootElement().getChild("prop", NS_DAV);
        if (elProp == null) {
            throw new RuntimeException("No prop element");
        }

        Set<QName> set = new HashSet<QName>();
        for (Object o : elProp.getChildren()) {
            if (o instanceof Element) {
                Element el = (Element) o;
                String local = el.getName();
                String ns = el.getNamespaceURI();
                set.add(new QName(ns, local, el.getNamespacePrefix()));
            }
        }
        return set;
    }
}
