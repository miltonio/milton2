/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */
package io.milton.http.acl;

import io.milton.http.caldav.CalDavProtocol;
import io.milton.http.caldav.CalendarQueryReport;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.report.Report;
import io.milton.http.report.ReportUtils;
import io.milton.http.webdav.PropFindPropertyBuilder;
import io.milton.http.webdav.PropFindResponse;
import io.milton.http.webdav.PropFindXmlGenerator;
import io.milton.http.webdav.PropertiesRequest;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.principal.DiscretePrincipal;
import io.milton.principal.PrincipalSearchCriteria;
import io.milton.principal.PrincipalSearchCriteria.SearchItem;
import io.milton.principal.PrincipalSearchService;
import io.milton.resource.PropFindableResource;
import io.milton.resource.Resource;

import java.util.*;

import javax.xml.namespace.QName;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The report can be used to search principals for matching properties and
 * return specified properties for the matching principal resources. To search
 * for an address book owned by a user named "Laurie", the REPORT request body
 * would look like this:
 * <code><pre>
&lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot; ?&gt;
&lt;D:principal-property-search xmlns:D=&quot;DAV:&quot;&gt;
&lt;D:property-search&gt;
&lt;D:prop&gt;
&lt;D:displayname/&gt;
&lt;/D:prop&gt;
&lt;D:match&gt;Laurie&lt;/D:match&gt;
&lt;/D:property-search&gt;
&lt;D:prop&gt;
&lt;C:addressbook-home-set xmlns:C=&quot;urn:ietf:params:xml:ns:carddav&quot;/&gt;
&lt;D:displayname/&gt;
&lt;/D:prop&gt;
&lt;/D:principal-property-search&gt;
 * </pre></code>
 * The server performs a case-sensitive or caseless search for a matching string
 * subset of "Laurie" within the DAV:displayname property. Thus, the server
 * might return "Laurie Dusseault", "Laurier Desruisseaux", or "Wilfrid Laurier"
 * all as matching DAV:displayname values, and the address books for each of
 * these.
 *
 * @author nabil.shams
 */
public class PrincipalPropertySearchReport implements Report {

    private static final Logger log = LoggerFactory.getLogger(CalendarQueryReport.class);
    private final PropFindPropertyBuilder propertyBuilder;
    private final PropFindXmlGenerator xmlGenerator;
    private final PrincipalSearchService principalSearchService;
    private final Namespace NS_DAV = Namespace.getNamespace(WebDavProtocol.NS_DAV.getPrefix(), WebDavProtocol.NS_DAV.getName());
    private final Namespace NS_CAL = Namespace.getNamespace("C", CalDavProtocol.CALDAV_NS);

    public PrincipalPropertySearchReport(PropFindPropertyBuilder propertyBuilder, PropFindXmlGenerator xmlGenerator, PrincipalSearchService principalSearchService) {
        this.propertyBuilder = propertyBuilder;
        this.xmlGenerator = xmlGenerator;
        this.principalSearchService = principalSearchService;
    }

    @Override
    public String getName() {
        return "principal-property-search";
    }

    @Override
    public String process(String host, String path, Resource r, Document doc) throws BadRequestException, NotAuthorizedException {
        log.debug("process");
        // The requested properties
        Set<QName> props = getProps(doc);

        PropertiesRequest parseResult = PropertiesRequest.toProperties(props);

        // Generate the response
        List<PropFindResponse> respProps = new ArrayList<>();
        PrincipalSearchCriteria crit = new PrincipalSearchCriteria();
        List<PrincipalSearchCriteria.SearchItem> searchTerms = new ArrayList<>();
        for (Element el : ReportUtils.findAll(doc.getRootElement(), "property-search", NS_DAV)) {
            String field = null;
            Element elProp = ReportUtils.find(el, "prop", NS_DAV);
            Iterator propIter = elProp.getContent(new ElementFilter()).iterator();
            if(propIter.hasNext()) {
               Element elPropChild = (Element) propIter.next();
               QName qname = new QName(elPropChild.getNamespaceURI(), elPropChild.getName());
               field = qname.toString();
               if(propIter.hasNext()) {
                  log.warn("Multiple children of property-search/prop element not supported");
               }
            } else {
               log.warn("property-search/prop element has no children");
            }
            Element elMatch = ReportUtils.find(el, "match", NS_DAV);
            PrincipalSearchCriteria.MatchType matchType = PrincipalSearchCriteria.MatchType.CONTAINS;
            String matchAttr = elMatch.getAttributeValue("match-type");
            if(matchAttr != null) {
               matchType = PrincipalSearchCriteria.MatchType.fromCode(matchAttr);
            }
            String matchVal = elMatch.getText();
            SearchItem item = new PrincipalSearchCriteria.SearchItem();
            item.setField(field);
            item.setMatchType(matchType);
            item.setValue(matchVal);
            searchTerms.add(item);
        }
        crit.setSearchItems(searchTerms);
        crit.setCuType(null); // TODO
        String typeAttr = doc.getRootElement().getAttributeValue("test");
        crit.setTest(PrincipalSearchCriteria.TestType.fromCode(typeAttr));

        List<DiscretePrincipal> foundResources = principalSearchService.search(crit, r);
        if (foundResources != null) {
            log.info("foundResources: " + foundResources.size());
        } else {
            log.info("foundResources: null");
        }
        for (DiscretePrincipal dp : foundResources) {
            String href = dp.getPrincipalURL();
            List<PropFindResponse> resps = new ArrayList<>();
            if (dp instanceof PropFindableResource) {
                PropFindableResource pfr = (PropFindableResource) dp;
                propertyBuilder.processResource(resps, pfr, parseResult, href, 0, 0, href);
            }

            respProps.addAll(resps);
        }

        String xml = xmlGenerator.generate(respProps);
        log.debug(xml);
        return xml;
    }

    private Set<QName> getProps(Document doc) {
        Element elProp = doc.getRootElement().getChild("prop", NS_DAV);
        if (elProp == null) {
            throw new RuntimeException("No prop element");
        }

        Set<QName> set = new HashSet<>();
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
