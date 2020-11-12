/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */

package io.milton.http.carddav;

import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.report.QualifiedReport;
import io.milton.http.report.ReportUtils;
import io.milton.http.webdav.PropFindPropertyBuilder;
import io.milton.http.webdav.PropFindResponse;
import io.milton.http.webdav.PropFindXmlGenerator;
import io.milton.http.webdav.PropertiesRequest;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.principal.PrincipalSearchCriteria;
import io.milton.principal.PrincipalSearchCriteria.TestType;
import io.milton.resource.AddressBookQuerySearchableResource;
import io.milton.resource.PropFindableResource;
import io.milton.resource.Resource;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The CARDDAV:addressbook-query REPORT performs a search for all address object
 * resources that match a specified filter. The response of this report will
 * contain all the WebDAV properties and address object resource data specified
 * in the request. In the case of the CARDDAV:address-data XML element, one can
 * explicitly specify the vCard properties that should be returned in the address
 * object resource data that matches the filter.
 *
 * The format of this report is modeled on the PROPFIND method. The request and
 * response bodies of the CARDDAV:addressbook-query report
 * use XML elements that are also used by PROPFIND. In particular, the
 * request can include XML elements to request WebDAV properties to be
 * returned. When that occurs, the response should follow the same
 * behavior as PROPFIND with respect to the DAV:multistatus response
 * elements used to return specific WebDAV property results. For
 * instance, a request to retrieve the value of a WebDAV property that
 * does not exist is an error and MUST be noted with a response XML
 * element that contains a 404 (Not Found) status value.
 *
 * @see http://tools.ietf.org/html/rfc6352#section-8.6

 * Examples :
 *   Input request is like :
 *   <code><pre>&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;C:addressbook-query xmlns:C="urn:ietf:params:xml:ns:carddav" xmlns:D="DAV:"&gt;
	&lt;D:prop&gt;
		&lt;D:getetag/&gt;
		&lt;C:address-data/&gt;
	&lt;/D:prop&gt;
	&lt;C:filter&gt;
		&lt;C:prop-filter name="mail"&gt;
			&lt;C:text-match collation="i;unicasemap" match-type="starts-with"&gt;Laurie&lt;/C:text-match&gt;
		&lt;/C:prop-filter&gt;
	&lt;/C:filter&gt;
&lt;/C:addressbook-query&gt;
</pre></code>

or more complex with many criterias

<code><pre>
&lt;?xml version="1.0" encoding="utf-8" ?&gt;
&lt;C:addressbook-query xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:carddav"&gt;
    &lt;D:prop&gt;
        &lt;D:getetag/&gt;
        &lt;C:address-data&gt;
            &lt;C:allprop/&gt;
        &lt;/C:address-data&gt;
    &lt;/D:prop&gt;
    &lt;C:filter test="anyof"&gt;
        &lt;C:prop-filter name="sn"&gt;
            &lt;C:text-match collation="i;unicode-casemap" negate-condition="no" match-type="contains"&gt;Laurie&lt;/C:text-match&gt;
        &lt;/C:prop-filter&gt;
        &lt;C:prop-filter name="givenname"&gt;
            &lt;C:text-match collation="i;unicode-casemap" negate-condition="no" match-type="contains"&gt;Laurie&lt;/C:text-match&gt;
        &lt;/C:prop-filter&gt;
        &lt;C:prop-filter name="email"&gt;
            &lt;C:text-match collation="i;unicode-casemap" negate-condition="no" match-type="contains"&gt;Laurie&lt;/C:text-match&gt;
        &lt;/C:prop-filter&gt;
    &lt;/C:filter&gt;
    &lt;C:limit&gt;
        &lt;C:nresults&gt;$limit&lt;/C:nresults&gt;
    &lt;/C:limit&gt;
&lt;/C:addressbook-query&gt;

</pre></code>

 * @author nabil.shams
 * @author charly-alinto
 * @date 10 sept. 2014
 */
public class AddressBookQueryReport implements QualifiedReport
{

	private static final Logger log = LoggerFactory.getLogger(AddressBookMultiGetReport.class);


    private final Namespace								NS_DAV			= Namespace.getNamespace( WebDavProtocol.NS_DAV.getPrefix(), WebDavProtocol.NS_DAV.getName() );
  	private final Namespace								CARDDAV_NS	= Namespace.getNamespace( "C", CardDavProtocol.CARDDAV_NS );

  	private final ResourceFactory					resourceFactory;
  	private final PropFindPropertyBuilder	propertyBuilder;
  	private final PropFindXmlGenerator		xmlGenerator;

  	public AddressBookQueryReport( ResourceFactory resourceFactory, PropFindPropertyBuilder propertyBuilder, PropFindXmlGenerator xmlGenerator )
  	{
  		this.resourceFactory = resourceFactory;
  		this.propertyBuilder = propertyBuilder;
  		this.xmlGenerator = xmlGenerator;
  	}

  	@Override
  	public String getName() {
  		return "addressbook-query";
  	}

    @Override
    public QName getQualifiedName()
    {
        return new QName(CARDDAV_NS.getURI(), getName());
    }

  	@Override
  	public String process( String host, String path, Resource res, Document doc )
  	{
  		log.trace( "AddressBookQueryReport.process() host=" + host + " path=" + path );
  		// The requested properties
  		Set<QName> props = this.getProps( doc );

  		PropertiesRequest parseResult = PropertiesRequest.toProperties( props );

  		PrincipalSearchCriteria crit = new PrincipalSearchCriteria();
  		List<Element> filters = ReportUtils.findAll( doc.getRootElement(), "filter", this.CARDDAV_NS );
  		if ( filters.size() >= 1 )
  		{
  			// many filters, check 'test' attribute, must be 'anyof' or 'allof'
  			crit.setTest( TestType.ANY );
  			Attribute attribute = filters.get( 0 ).getAttribute( "test" );
  			if ( attribute != null )
  			{
  				String filterMatchType = attribute.getValue();
  				// set filterMatchType to Criterias
  				if ( "allof".equalsIgnoreCase( filterMatchType ) )
  				{
  					crit.setTest( TestType.ALL );
  				}
  			}
  		}

  		List<PrincipalSearchCriteria.SearchItem> searchTerms = new ArrayList<>();
  		List<Element> propFilters = ReportUtils.findAll( doc.getRootElement(), "prop-filter", this.CARDDAV_NS );
  		for ( Element propFilter : propFilters )
  		{
  			String propFilterName = propFilter.getAttribute( "name" ).getValue();
  			List<Element> textMatchs = ReportUtils.findAll( propFilter, "text-match", this.CARDDAV_NS );
  			for ( Element textMath : textMatchs )
  			{
  				// textMatchType can be "contains", "equals",  "starts-with", "ends-with"...
  				String textMatchType = textMath.getAttribute( "match-type" ).getValue();
  				// matchVal get the text to search
  				String matchVal = textMath.getText();

  				// create criteria
  				PrincipalSearchCriteria.SearchItem item = new PrincipalSearchCriteria.SearchItem();
  				item.setField( propFilterName );
  				item.setMatchType( PrincipalSearchCriteria.MatchType.fromCode( textMatchType ) );
  				item.setValue( matchVal );
  				// add criteria
  				searchTerms.add( item );
  			}
  		}
  		crit.setSearchItems( searchTerms );

  		try
  		{
  			Resource resource = this.resourceFactory.getResource( host, path );
  			if ( resource instanceof AddressBookQuerySearchableResource )
  			{
  				// Do the search
  				log.debug( "resource is AddressBookQuerySearchableResource" );
  				AddressBookQuerySearchableResource searchableAddressBook = (AddressBookQuerySearchableResource)resource;
  				List<? extends Resource> result = searchableAddressBook.getChildren( crit );

  				// Generate the response
  				List<PropFindResponse> respProps = new ArrayList<>();
  				for ( Resource r : result )
  				{
  					if ( r != null )
  					{
  						if ( r instanceof PropFindableResource )
  						{
  							PropFindableResource pfr = (PropFindableResource)r;
  							try
  							{
  								respProps.addAll( this.propertyBuilder.buildProperties( pfr, 0, parseResult, path + r.getUniqueId() ) );
  							}
  							catch( URISyntaxException ex )
  							{
  								throw new RuntimeException( "There was an unencoded url requested: " + path + r.getUniqueId(), ex );
  							}
  						}
  					}
  				}

  				String xml = this.xmlGenerator.generate( respProps );
  				log.trace( "AddressBookQueryReport result:\n" + xml );
  				return xml;
  			}
  			else
  			{
  				log.warn( "You must implement AddressBookQuerySearchableResource to support CardDAV addressbook-query" );
  			}
  		}
  		catch( NotAuthorizedException | BadRequestException e )
  		{
  			log.error( "ERROR occured in AddressBookQueryReport.process", e );
  		}
		return "";
  	}

  	private Set<QName> getProps( Document doc )
  	{
  		Element elProp = doc.getRootElement().getChild( "prop", this.NS_DAV );
  		if ( elProp == null )
  		{
  			throw new RuntimeException( "No prop element" );
  		}

  		Set<QName> set = new HashSet<>();
  		for ( Object o : elProp.getChildren() )
  		{
  			if ( o instanceof Element )
  			{
  				Element el = (Element)o;
  				String local = el.getName();
  				String ns = el.getNamespaceURI();
  				set.add( new QName( ns, local, el.getNamespacePrefix() ) );
  			}
  		}
  		return set;
  	}
}
