/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */

package io.milton.http.caldav;

import io.milton.http.ResourceFactory;
import io.milton.http.report.AbstractMultiGetReport;
import io.milton.http.webdav.PropFindPropertyBuilder;
import io.milton.http.webdav.PropFindXmlGenerator;

/**
 *
 * @author brad
 */
public class MultiGetReport extends AbstractMultiGetReport {

    public MultiGetReport(ResourceFactory resourceFactory, PropFindPropertyBuilder propertyBuilder, PropFindXmlGenerator xmlGenerator) {
        super(resourceFactory, propertyBuilder, xmlGenerator);
    }

    @Override
    public String getName() {
        return "calendar-multiget";
    }
}
