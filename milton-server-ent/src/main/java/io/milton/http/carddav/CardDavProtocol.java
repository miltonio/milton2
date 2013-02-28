/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */

package io.milton.http.carddav;

import io.milton.principal.CardDavPrincipal;
import io.milton.http.Auth;
import io.milton.http.Handler;
import io.milton.http.HandlerHelper;
import io.milton.http.HttpExtension;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.ResourceFactory;
import io.milton.http.WellKnownResourceFactory.WellKnownHandler;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.http.http11.CustomPostHandler;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.http.values.HrefList;
import io.milton.http.values.ValueWriters;
import io.milton.http.webdav.PropFindXmlGenerator;
import io.milton.http.webdav.PropertyMap;
import io.milton.http.webdav.PropertyMap.StandardProperty;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.http.webdav.WebDavResponseHandler;
import io.milton.property.PropertySource;
import io.milton.common.LogUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.milton.resource.AddressResource;
import io.milton.resource.AddressBookResource;
import io.milton.http.values.Pair;
import io.milton.http.values.AddressDataTypeList;
import io.milton.http.webdav.PropFindPropertyBuilder;
import io.milton.http.caldav.ExpandPropertyReport;
import io.milton.resource.DigestResource;
import io.milton.resource.GetableResource;
import io.milton.resource.PropFindableResource;
import io.milton.resource.Resource;

/**
 *
 * @author bradm
 */
public class CardDavProtocol implements HttpExtension, PropertySource, WellKnownHandler {

    private static final Logger log = LoggerFactory.getLogger(CardDavProtocol.class);
    // Standard caldav properties
    public static final String CARDDAV_NS = "urn:ietf:params:xml:ns:carddav";
    private final Set<Handler> handlers;
    private final PropertyMap propertyMapCardDav;

    public CardDavProtocol(ResourceFactory resourceFactory, WebDavResponseHandler responseHandler, HandlerHelper handlerHelper, WebDavProtocol webDavProtocol, PropFindXmlGenerator gen, PropFindPropertyBuilder propertyBuilder) {
        propertyMapCardDav = new PropertyMap(CARDDAV_NS);
        propertyMapCardDav.add(new AddressBookHomeSetProperty());
        propertyMapCardDav.add(new AddressBookDescriptionProperty());
        propertyMapCardDav.add(new SupportedAddressData());
        propertyMapCardDav.add(new PrincipalAddress());
        propertyMapCardDav.add(new AddressDataProperty());

        handlers = new HashSet<Handler>();

        webDavProtocol.addPropertySource(this);
        
        webDavProtocol.addReport(new AddressBookMultiGetReport(resourceFactory, propertyBuilder, gen));
        webDavProtocol.addReport(new AddressBookQueryReport());
        webDavProtocol.addReport(new ExpandPropertyReport(resourceFactory, propertyBuilder, gen));
        webDavProtocol.addReport(new PrincipalPropertySearchReport());
    }

    @Override
    public Set<Handler> getHandlers() {
        return Collections.unmodifiableSet(handlers);
    }

    //TODO: remove debug logging once it's working
    @Override
    public Object getProperty(QName name, Resource r) {
        log.trace("getProperty: {}", name.getLocalPart());
        Object o;
        if (propertyMapCardDav.hasProperty(name)) {
            o = propertyMapCardDav.getProperty(name, r);
        } else {
            o = null;
        }
        log.debug("result : " + o);
        return o;
    }

    @Override
    public void setProperty(QName name, Object value, Resource r) {
        log.trace("setProperty: {}", name.getLocalPart());
        if (propertyMapCardDav.hasProperty(name)) {
            propertyMapCardDav.setProperty(name, r, value);
        }
    }

    @Override
    public PropertyMetaData getPropertyMetaData(QName name, Resource r) {
        PropertyMetaData md;
        if (propertyMapCardDav.hasProperty(name)) {
            md = propertyMapCardDav.getPropertyMetaData(name, r);
        } else {
            md = null;
        }
        log.trace("getPropertyMetaData: {} - returned: {}", name.getLocalPart(), md);
        return md;
    }

    @Override
    public void clearProperty(QName name, Resource r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<QName> getAllPropertyNames(Resource r) {
        log.trace("getAllPropertyNames");
        List<QName> list = new ArrayList<QName>();
        list.addAll(propertyMapCardDav.getAllPropertyNames(r));
        return list;
    }

    @Override
    public List<CustomPostHandler> getCustomPostHandlers() {
        return null;
    }
    
    // (CARDDAV:supported-address-data-conversion -- requires implemenation
    /**
     * When used in an address book REPORT request, the CARDDAV:address-data XML 
     * element specifies which parts of address object resources need to be 
     * returned in the response. If the CARDDAV:address-data XML element doesn’t 
     * contain any CARDDAV:prop elements, address object resources will be 
     * returned in their entirety. Additionally, a media type and version can be 
     * specified to request that the server return the data in that format if possible.
     * Finally, when used in an address book REPORT response, the CARDDAV:address-data 
     * XML element specifies the content of an address object resource. Given that 
     * XML parsers normalize the two-character sequence CRLF (US-ASCII decimal 13 and US-ASCII
     * decimal 10) to a single LF character (US-ASCII decimal 10), the CR
     * character (US-ASCII decimal 13) MAY be omitted in address object
     * resources specified in the CARDDAV:address-data XML element.
     * Furthermore, address object resources specified in the
     * CARDDAV:address-data XML element MAY be invalid per their media
     * type specification if the CARDDAV:address-data XML element part of
     * the address book REPORT request did not specify required vCard
     * properties (e.g., UID, etc.) or specified a CARDDAV:prop XML
     * element with the "novalue" attribute set to "yes".
     * 
     * Note: The CARDDAV:address-data XML element is specified in requests
     * and responses inside the DAV:prop XML element as if it were a
     * WebDAV property. However, the CARDDAV:address-data XML element is
     * not a WebDAV property and as such it is not returned in PROPFIND
     * responses nor used in PROPPATCH requests.
     * 
     * Note: The address data embedded within the CARDDAV:address-data XML
     * element MUST follow the standard XML character data encoding
     * rules, including use of &lt;, &gt;, &amp; etc., entity encoding or
     * the use of a <![CDATA[ ... ]]> construct. In the latter case, the
     * vCard data cannot contain the character sequence "]]>", which is
     * the end delimiter for the CDATA section.
     * 
     * Definition:
     * <!ELEMENT address-data (allprop | prop*)>
     * when nested in the DAV:prop XML element in an address book
     * REPORT request to specify which parts of address object
     * resources should be returned in the response;
     * <!ELEMENT address-data (#PCDATA)>
     * <!-- PCDATA value: address data -->
     * when nested in the DAV:prop XML element in an address book
     * REPORT response to specify the content of a returned
     * address object resource.
     * <!ATTLIST address-data content-type CDATA "text/vcard"
     * version CDATA "3.0">
     * <!-- content-type value: a MIME media type -->
     * <!-- version value: a version string -->
     * attributes can be used on each variant of the
     * CALDAV:address-data XML element.
     */
    class AddressDataProperty implements StandardProperty<String> {
        @Override
        public String fieldName() {
            return "address-data";
        }
        @Override
        public String getValue(PropFindableResource res) {
            if (res instanceof AddressResource) {
                AddressResource resource = (AddressResource) res;
                return resource.getAddressData(); 
            } else {
                return null;
            }
        }

        @Override
        public Class<String> getValueClass() {
            return String.class;
        }
    }

    /**
     * This property is meant to allow users to easily find the address book 
     * collections owned by the principal. Typically, users will group all the 
     * address book collections that they own under a common collection. This
     * property specifies the URL of collections that are either address
     * book collections or ordinary collections that have child or
     * descendant address book collections owned by the principal.
     * 
     * Definition:
     * <!ELEMENT addressbook-home-set (DAV:href*)>
     * 
     * Example:
     * <C:addressbook-home-set xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:carddav">
     *      <D:href>/bernard/addresses/</D:href>
     * </C:addressbook-home-set>
     */
    class AddressBookHomeSetProperty implements StandardProperty<HrefList> {

        @Override
        public String fieldName() {
            return "addressbook-home-set";
        }

        @Override
        public HrefList getValue(PropFindableResource res) {
            if (res instanceof CardDavPrincipal) {
                return ((CardDavPrincipal) res).getAddressBookHomeSet();
            } else {
                return null;
            }
        }

        @Override
        public Class<HrefList> getValueClass() {
            return HrefList.class;
        }
    }

    /*
     * This property contains a description of the address book collection that 
     * is suitable for presentation to a user. The xml:lang attribute can be 
     * used to add a language tag for the value of this property.
     * 
     * Definition: 
     * <!ELEMENT addressbook-description (#PCDATA)>
     * <!-- PCDATA value: string -->
     * 
     * Example:
     * <C:addressbook-description xml:lang="fr-CA"
     * xmlns:C="urn:ietf:params:xml:ns:carddav">Adresses de Oliver Daboo</C:addressbook-description>
     */
    class AddressBookDescriptionProperty implements StandardProperty<String> {
        // todo - add support of internationalization so the protocol can allow
        // multiple language description, this can be accomplished by either 
        // add a method(getAttributes) to StandardProperty interface or we can 
        // have an InternationalizedStandardProperty interface that would extend
        // from StandardProperty and have an additional method (getLanguage)
        @Override
        public String fieldName() {
            return "addressbook-description";
        }

        @Override
        public String getValue(PropFindableResource res) {
            if (res instanceof AddressBookResource) {
                AddressBookResource addressBookResource = (AddressBookResource) res;
                return addressBookResource.getDescription().getValue();
            } else {
                return null;
            }
        }

        @Override
        public Class<String> getValueClass() {
            return String.class;
        }
    }

    /**
     * This property is used to specify the media type supported for the address 
     * object resources contained in a given address book collection (e.g., 
     * vCard version 3.0). Any attempt by the client to store address object 
     * resources with a media type not listed in this property MUST result in 
     * an error, with the CARDDAV:supported-address-data precondition 
     * (Section 6.3.2.1) being violated. In the absence of this property, 
     * the server MUST only accept data with the media type "text/vcard" and 
     * vCard version 3.0, and clients can assume that is all the server will accept.
     * 
     * Definition:
     * <!ELEMENT supported-address-data (address-data-type+)>
     *      <!ELEMENT address-data-type EMPTY>
     * <!ATTLIST address-data-type content-type CDATA "text/vcard" version CDATA "3.0">
     * <!-- content-type value: a MIME media type -->
     * <!-- version value: a version string -->
     * 
     * Example:
     * <C:supported-address-data xmlns:C="urn:ietf:params:xml:ns:carddav">
     *      <C:address-data-type content-type="text/vcard" version="3.0"/>
     * </C:supported-address-data>
     */
    class SupportedAddressData implements StandardProperty<List<Pair<String, String>>> {

        @Override
        public String fieldName() {
            return "supported-address-data";
        }

        @Override
        public List<Pair<String, String>> getValue(PropFindableResource res) {
            if (res instanceof AddressBookResource) {
                return ((AddressBookResource) res).getSupportedAddressData();
            } else {
                return null;
            }
        }

        @Override
        public Class<AddressDataTypeList> getValueClass() {
            return AddressDataTypeList.class;
        }
    }

    /**
     * This property is used to specify a numeric value that represents the 
     * maximum size in octets that the server is willing to accept when an 
     * address object resource is stored in an address book collection. 
     * Any attempt to store an address book object resource exceeding this 
     * size MUST result in an error, with the CARDDAV:max-resource-size precondition
     * (Section 6.3.2.1) being violated. In the absence of this
     * property, the client can assume that the server will allow storing
     * a resource of any reasonable size.
     * 
     * Definition:
     * <!ELEMENT max-resource-size (#PCDATA)>
     * <!-- PCDATA value: a numeric value (positive decimal integer) -->
     * 
     * Example:
     * <C:max-resource-size xmlns:C="urn:ietf:params:xml:ns:carddav">102400</C:max-resource-size>
     * 
     */
    class MaxResourceSize implements StandardProperty<Long> {

        @Override
        public String fieldName() {
            return "max-resource-size";
        }

        @Override
        public Long getValue(PropFindableResource res) {
            if (res instanceof AddressBookResource) {
                return ((AddressBookResource) res).getMaxResourceSize();
            } else {
                return null;
            }
        }

        @Override
        public Class<Long> getValueClass() {
            return Long.class;
        }
    }
    
    /**
     * This property is meant to allow users to easily find contact information 
     * for users represented by principals on the system. This property specifies
     * the URL of the resource containing the corresponding contact
     * information. The resource could be an address object resource in
     * an address book collection, or it could be a resource in a
     * "regular" collection.
     * 
     * Definition:
     * <!ELEMENT principal-address (DAV:href)>
     * 
     * Example:
     * <C:principal-address xmlns:D="DAV:"xmlns:C="urn:ietf:params:xml:ns:carddav">
     *      <D:href>/system/cyrus.vcf</D:href>
     * </C:principal-address>
     * 
     */
    class PrincipalAddress implements StandardProperty<String> {

        @Override
        public String fieldName() {
            return "principal-address";
        }

        @Override
        public String getValue(PropFindableResource res) {
            if (res instanceof CardDavPrincipal) {
                return ((CardDavPrincipal) res).getAddress();
            } else {
                return null;
            }
        }

        @Override
        public Class<Long> getValueClass() {
            return Long.class;
        }
    }
    
    

    @Override
    public String getWellKnownName() {
        return "carddav";
    }

    @Override
    public Resource locateWellKnownResource(Resource host) {
        log.trace("found a carddav well-known resource");
        return new CardDavWellKnownResource(host);
    }

    public class CardDavWellKnownResource implements DigestResource, GetableResource, PropFindableResource {

        private final Resource host;

        public CardDavWellKnownResource(Resource host) {
            this.host = host;
        }

        @Override
        public String getUniqueId() {
            return null;
        }

        @Override
        public String getName() {
            return getWellKnownName();
        }

        @Override
        public Object authenticate(String user, String password) {
            return host.authenticate(user, password);
        }

        @Override
        public boolean authorise(Request request, Method method, Auth auth) {
            // we require a user, so we know where to redirect to
            return (auth != null);
        }

        @Override
        public String getRealm() {
            return host.getRealm();
        }

        @Override
        public Date getModifiedDate() {
            return null; // no caching
        }

        @Override
        public String checkRedirect(Request request) {
            log.trace("well-known: checkRedirect");
            Auth auth = request.getAuthorization();
            HrefList addressBookHomes;
            String first;
            if (auth != null && auth.getTag() != null) {
                if (auth.getTag() instanceof CardDavPrincipal) {
                    CardDavPrincipal p = (CardDavPrincipal) auth.getTag();
                    addressBookHomes = p.getAddressBookHomeSet();
                    if (addressBookHomes == null || addressBookHomes.isEmpty()) {
                        log.error("can't redirect, CalDavPrincipal.getCalendatHomeSet did not return an address. Check implementation class: " + p.getClass());
                        return null;
                    } else {
                        first = addressBookHomes.get(0); // just use first
                        LogUtils.debug(log, "well-known: checkRedirect. redirecting to:", first);
                        return first;
                    }
                } else {
                    log.warn("can't redirect, auth.getTag is not a CardDavPrincipal, is a: " + auth.getTag().getClass() + " To use CARDDAV, the user object returned from authenticate must be a " + CardDavPrincipal.class);
                    return null;
                }
            } else {
                log.trace("can't redirect, no authorisation");
                return null;
            }
        }

        @Override
        public Object authenticate(DigestResponse digestRequest) {
            if (host instanceof DigestResource) {
                DigestResource dr = (DigestResource) host;
                return dr.authenticate(digestRequest);
            } else {
                return null;
            }
        }

        @Override
        public boolean isDigestAllowed() {
            if (host instanceof DigestResource) {
                DigestResource dr = (DigestResource) host;
                return dr.isDigestAllowed();
            } else {
                return false;
            }
        }

        @Override
        public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Long getMaxAgeSeconds(Auth auth) {
            return null; // no caching
        }

        @Override
        public String getContentType(String accepts) {
            return null;
        }

        @Override
        public Long getContentLength() {
            return null;
        }

        @Override
        public Date getCreateDate() {
            return null;
        }
    }
}