/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */

package io.milton.http.carddav;

import io.milton.http.report.Report;
import io.milton.resource.Resource;
import org.jdom.Document;

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
 * @author nabil.shams
 */
public class AddressBookQueryReport implements Report {

    @Override
    public String getName() {
        return "addressbook-query";
    }

    @Override
    public String process(String host, String path, Resource r, Document doc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
