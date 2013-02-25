/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * 
 */

package com.ettrema.http.caldav;

import java.net.URI;
import java.net.URISyntaxException;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class CalDavPropertySourceTest extends TestCase {

    public void testGetProperty() throws URISyntaxException {
        URI uri = new URI( "mailto:bernard@example.com" );
        URI[] arr = new URI[1];
        arr[0] = uri;
        if( arr instanceof URI[]) {
            System.out.println( "hiii" );
        }
    }

    public void testSetProperty() {
    }

    public void testGetPropertyMetaData() {
    }

    public void testClearProperty() {
    }

    public void testGetAllPropertyNames() {
    }
}
