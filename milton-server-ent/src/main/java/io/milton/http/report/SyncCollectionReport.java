package io.milton.http.report;

import io.milton.common.Utils;
import io.milton.http.HttpManager;
import io.milton.http.Response;
import io.milton.http.XmlWriter;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.webdav.PropFindPropertyBuilder;
import io.milton.http.webdav.PropFindResponse;
import io.milton.http.webdav.PropFindXmlFooter;
import io.milton.http.webdav.PropFindXmlGenerator;
import io.milton.http.webdav.PropertiesRequest;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.resource.PropFindableResource;
import io.milton.resource.RemovedResource;
import io.milton.resource.Resource;
import io.milton.resource.SyncCollectionResource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

public class SyncCollectionReport implements Report {
    protected final Namespace NS_DAV = Namespace.getNamespace(WebDavProtocol.NS_DAV.getPrefix(), WebDavProtocol.NS_DAV.getName());
    private static enum SyncLevel {
      One,
      Infinite
    }
    private final PropFindPropertyBuilder propertyBuilder;
    private final PropFindXmlGenerator xmlGenerator;
    
    public SyncCollectionReport(PropFindPropertyBuilder propertyBuilder, PropFindXmlGenerator xmlGenerator) {
        this.propertyBuilder = propertyBuilder;
        this.xmlGenerator = xmlGenerator;
    }
  
    @Override
    public String getName() {
      return "sync-collection";
    }
  
    @Override
    public String process(String host, String path, Resource r, Document doc)
        throws BadRequestException, NotAuthorizedException {
        if (!(r instanceof SyncCollectionResource)) {
            throw new BadRequestException(r, "This resource does not support sync-token.");
        }
        SyncCollectionResource syncCollectionResource = (SyncCollectionResource) r;
      
        /*
         * This report is only defined when the Depth header has value "0";
          other values result in a 400 (Bad Request) error response.  Note
          that [RFC3253], Section 3.6, states that if the Depth header is
          not present, it defaults to a value of "0".
        
          3.3.  Depth Behavior
  
           Servers MUST support only Depth:0 behavior with the
           DAV:sync-collection report, i.e., the report targets only the
           collection being synchronized in a single request.
        */
        if (HttpManager.request().getDepthHeader() != 0) {
        //throw new BadRequestException(r, "Depth header must be 0");
        // iOS 8.1 sends depth=1 with sync-collection, so ignore the spec
        }
      
        /* However, clients
        do need to "scope" the synchronization to different levels within
        that collection -- specifically, immediate children (level "1") and
        all children at any depth (level "infinite").  To specify which level
        to use, clients MUST include a DAV:sync-level XML element in the
        request. */
        Element syncLevelElm = ReportUtils.find(doc.getRootElement(), "sync-level", NS_DAV);
        /* To specify which level
        to use, clients MUST include a DAV:sync-level XML element in the
        request.*/
        if (syncLevelElm == null) {
            throw new BadRequestException(r, "DAV:sync-level must be included in the request.");
        }
      
        String syncLevel = syncLevelElm.getText(); // 1 or infinite
        SyncLevel lv = SyncLevel.One;
        if ("1".equals(syncLevel)) {
            /* When the client specifies the DAV:sync-level XML element with a
            value of "1", only appropriate internal member URLs (immediate
            children) of the collection specified as the request-URI are
            reported. */
            lv = SyncLevel.One;
        } else if ("infinite".equals(syncLevel)) {
            /* When the client specifies the DAV:sync-level XML element with a
            value of "infinite", all appropriate member URLs of the collection
            specified as the request-URI are reported, provided child
            collections themselves also support the DAV:sync-collection
            report. */
            lv = SyncLevel.Infinite;
        } else {
            throw new BadRequestException(r, String.format("Unsupported DAV:sync-level: \"%s\", must be either \"1\" or \"infinite\".", syncLevel));
        }

        URI syncToken = null;
        Element syncTokenElm = ReportUtils.find(doc.getRootElement(), "sync-token", NS_DAV);
        if (syncTokenElm != null)
        {
            String syncTokenText = syncTokenElm.getText();
            if (syncTokenText != null && !syncTokenText.isEmpty())
            {
                try
                {
                    syncToken = new URI(syncTokenText);
                }
                catch (URISyntaxException e)
                {
                    throw new BadRequestException(r, "sync-token must be a valid URI.");
                }
            }
        }

        String parentHref = HttpManager.request().getAbsolutePath();
        parentHref = Utils.suffixSlash(parentHref);
        List<PropFindResponse> respProps = new ArrayList<PropFindResponse>();
        findResources(syncCollectionResource, doc, syncToken, lv, parentHref, respProps);

        final URI nextSyncToken = syncCollectionResource.getSyncToken();
        /*
          <A:sync-collection xmlns:A="DAV:">
            <A:sync-token>http://example.org/sync/1414342005182</A:sync-token>
            <A:sync-level>1</A:sync-level>
            <A:prop>
              <A:getetag/>
              <A:getcontenttype/>
            </A:prop>
          </A:sync-collection>
        */
      
        /*For members that have changed (i.e., are new or have had their
           mapped resource modified), the DAV:response MUST contain at
           least one DAV:propstat element and MUST NOT contain any
           DAV:status element.
  
           For members that have been removed, the DAV:response MUST
           contain one DAV:status with a value set to '404 Not Found' and
           MUST NOT contain any DAV:propstat element.
  
           For members that are collections and are unable to support the
           DAV:sync-collection report, the DAV:response MUST contain one
           DAV:status with a value set to '403 Forbidden', a DAV:error
           containing DAV:supported-report or DAV:sync-traversal-supported
           (see Section 3.3 for which is appropriate) and MUST NOT contain
           any DAV:propstat element.
         */
      
        //List<PropFindResponse>
      
        String xml = xmlGenerator.generate(respProps, new PropFindXmlFooter() {
            @Override
            public void footer(XmlWriter writer) {
                writer.writeProperty(WebDavProtocol.NS_DAV.getPrefix(), "sync-token", nextSyncToken.toString());
            }
        });
      
        return xml;
    }
  
    private void findResources(SyncCollectionResource parent, Document doc, URI syncToken, SyncLevel syncLevel, String parentHref, List<PropFindResponse> respProps) throws NotAuthorizedException, BadRequestException {
        Map<String, Resource> children = parent.findResourcesBySyncToken(syncToken);
        for (String href : children.keySet()) {
            Resource r = children.get(href);
            //hrefs.add(parentHref + r.getName());
      
            if (r instanceof RemovedResource) {
                /* 3.4.  Types of Changes Reported on Initial Synchronization

                When the DAV:sync-collection request contains an empty DAV:sync-token
                element, the server MUST return all member URLs of the collection
                (taking account of the DAV:sync-level XML element value as per
                Section 3.3, and optional truncation of the result set as per
                Section 3.6) and it MUST NOT return any removed member URLs.  All
                types of member (collection or non-collection) MUST be reported.
                */
                if (syncToken != null) {
                    /* For members that have been removed, the DAV:response MUST
                    contain one DAV:status with a value set to '404 Not Found' and
                    MUST NOT contain any DAV:propstat element. */
                    PropFindResponse resp = new PropFindResponse(href, Response.Status.SC_NOT_FOUND);
                    
                    // TODO 404 must be in just under the response
                    respProps.add(resp);
                }
            } else if (r instanceof PropFindableResource) {
                Set<QName> props = getProps(doc);
                PropertiesRequest parseResult = PropertiesRequest.toProperties(props);
                
                PropFindableResource pfr = (PropFindableResource) r;
                try {
                    respProps.addAll(propertyBuilder.buildProperties(pfr, 0, parseResult, href));
                } catch (URISyntaxException ex) {
                    throw new RuntimeException("There was an unencoded url requested: " + href, ex);
                }
            } else {
                //log.warn("requested href is for a non PropFindableResource: " + r.getClass() + " - " + href);
            }
            
            if (syncLevel == SyncLevel.Infinite) {
                /* When the client specifies the DAV:sync-level XML element with a
                value of "infinite", all appropriate member URLs of the collection
                specified as the request-URI are reported, provided child
                collections themselves also support the DAV:sync-collection
                report. */
      	        if (r instanceof SyncCollectionResource) {
                    String currentHref = Utils.suffixSlash(parentHref + r.getName()); 
                    findResources((SyncCollectionResource) r, doc, syncToken, syncLevel, currentHref, respProps);
                }
                /* For members that are collections and are unable to support the
                DAV:sync-collection report, the DAV:response MUST contain one
                DAV:status with a value set to '403 Forbidden', a DAV:error
                containing DAV:supported-report or DAV:sync-traversal-supported
                (see Section 3.3 for which is appropriate) and MUST NOT contain
                any DAV:propstat element.
                */
            }
        }
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
