/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */

package io.milton.http.carddav;

import io.milton.http.ResourceFactory;
import io.milton.http.caldav.MultiGetReport;
import io.milton.http.webdav.PropFindPropertyBuilder;
import io.milton.http.webdav.PropFindXmlGenerator;

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
public class AddressBookMultiGetReport extends MultiGetReport {

    public AddressBookMultiGetReport(ResourceFactory resourceFactory, PropFindPropertyBuilder propertyBuilder, PropFindXmlGenerator xmlGenerator) {
        super(resourceFactory, propertyBuilder, xmlGenerator);
    }

    @Override
    public String getName() {
        return "addressbook-multiget";
    }
}
