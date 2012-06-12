/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.milton.httpclient.calsync.parse;

import io.milton.httpclient.parse.PropertyAccessor;
import java.lang.reflect.Method;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class TypeConverterTest extends TestCase {
    
    PropertyAccessor typeConverter;

    @Override
    protected void setUp() throws Exception {
        typeConverter = new PropertyAccessor();
    }
    
        
    public void test_GetString() throws Exception{
        MyCalendarEventBean bean = new MyCalendarEventBean();
        bean.setSummary("XXX");
        Method readMethod = bean.getClass().getMethod("getSummar");
        String v = typeConverter.get(bean, readMethod, String.class);
        assertEquals("XXX", v);
    }
    
    public void test_SetString() throws Exception{
        MyCalendarEventBean bean = new MyCalendarEventBean();
        bean.setSummary("XXX");
        Method writeMethod = bean.getClass().getMethod("setSummary", String.class);
        typeConverter.set(bean, writeMethod, "YYY");
        assertEquals("YYY", bean.getSummary());
    }    
}
