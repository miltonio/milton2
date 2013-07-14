/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */

package io.milton.http.caldav;

import io.milton.http.HttpManager;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.report.Report;
import io.milton.http.webdav.PropFindPropertyBuilder;
import io.milton.http.webdav.PropFindResponse;
import io.milton.http.webdav.PropFindXmlGenerator;
import io.milton.http.webdav.PropertiesRequest;
import io.milton.http.webdav.WebDavProtocol;
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
 *
 * @author brad
 */
public class MultiGetReport implements Report {

    private static final Logger log = LoggerFactory.getLogger(MultiGetReport.class);
    private final ResourceFactory resourceFactory;
    private final PropFindPropertyBuilder propertyBuilder;
    private final PropFindXmlGenerator xmlGenerator;
    private final Namespace NS_DAV = Namespace.getNamespace(WebDavProtocol.NS_DAV.getPrefix(), WebDavProtocol.NS_DAV.getName());

    public MultiGetReport(ResourceFactory resourceFactory, PropFindPropertyBuilder propertyBuilder, PropFindXmlGenerator xmlGenerator) {
        if( resourceFactory == null ) {
            throw new NullPointerException("resourceFactory is null");
        }
        this.resourceFactory = resourceFactory;
        this.propertyBuilder = propertyBuilder;
        this.xmlGenerator = xmlGenerator;
    }

    @Override
    public String getName() {
        return "calendar-multiget";
    }

    @Override
    public String process(String host, String path, Resource calendar, Document doc) throws NotAuthorizedException, BadRequestException {
        log.debug("process report: " + getName());
        // The requested properties
        Set<QName> props = getProps(doc);
        // The requested resources
        List<String> hrefs = getHrefs(doc);

        PropertiesRequest parseResult = PropertiesRequest.toProperties(props);

        // Generate the response
        List<PropFindResponse> respProps = new ArrayList<PropFindResponse>();

        for (String href : hrefs) {
            String decodedHref = HttpManager.decodeUrl( href );
            Resource r = resourceFactory.getResource(host, decodedHref);
            if (r != null) {
                if (r instanceof PropFindableResource) {
                    PropFindableResource pfr = (PropFindableResource) r;
                    try {
                        respProps.addAll(propertyBuilder.buildProperties(pfr, 0, parseResult, href));
                    } catch (URISyntaxException ex) {
                        throw new RuntimeException("There was an unencoded url requested: " + href, ex);
                    }
                } else {
                    log.warn("requested href is for a non PropFindableResource: " + r.getClass() + " - " + href);
                }
            } else {
                log.warn("requested href not found: " + href + " on resource factory: " + resourceFactory.getClass());
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
                    String href = el.getText();
                    list.add(href);
                    if( log.isTraceEnabled()) {
                        log.trace(" href: " + href);
                    }
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
