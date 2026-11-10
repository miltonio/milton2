/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * 
 */

package com.ettrema.http.caldav;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

/**
 *
 * @author brad
 */
public class CalDavPropertySourceTest {

    @Test
    public void testGetProperty() throws URISyntaxException {
        URI uri = new URI( "mailto:bernard@example.com" );
        URI[] arr = new URI[1];
        arr[0] = uri;
        if( arr instanceof URI[]) {
            System.out.println( "hiii" );
        }
    }

    @Test
    public void testSetProperty() {
    }

    @Test
    public void testGetPropertyMetaData() {
    }

    @Test
    public void testClearProperty() {
    }

    @Test
    public void testGetAllPropertyNames() {
    }
}
