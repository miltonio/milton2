/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.http;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class BeanCookieTest extends TestCase {
    
    public BeanCookieTest(String testName) {
        super(testName);
    }

    public void testFormat_Simple()  {
        BeanCookie bc = new BeanCookie("name");                
        bc.setValue("value");
        assertEquals("name=value", BeanCookie.toHeader(bc));
    }
    
    
    public void testFormat_Complex() throws DateUtils.DateParseException, DateUtils.DateParseException {
        BeanCookie bc = new BeanCookie("SSID");                
        bc.setValue("XYZ");
        bc.setDomain(".foo.com");
        bc.setPath("/");
        bc.setExpiry(1);
        bc.setSecure(true);
        bc.setHttpOnly(true);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(Calendar.DATE, 13);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.YEAR, 2021);
        cal.set(Calendar.HOUR_OF_DAY, 22);
        cal.set(Calendar.MINUTE, 23);
        cal.set(Calendar.SECOND, 0);        
        Date now = cal.getTime();
        System.out.println("now=" + now);
        String h = BeanCookie.toHeader(bc, now.getTime());
        System.out.println("h=" + h);
        assertEquals("SSID=XYZ; Domain=.foo.com; Path=/; Expires=Wed, 13-Jan-2021 22:23:01 GMT; Secure; HttpOnly", h);
    }    
}
