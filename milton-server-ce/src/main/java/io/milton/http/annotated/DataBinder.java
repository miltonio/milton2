/*
 * Copyright (C) 2012 McEvoy Software Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.milton.http.annotated;

import io.milton.common.Formatter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean2;
import org.apache.commons.beanutils.converters.BigDecimalConverter;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author brad
 */
public class DataBinder {

	/**
	 * Trims to null, so will never return a value padded with white space, or only whitespace
	 * 
	 * @param props
	 * @param name
	 * @return 
	 */
	public static String getRawParam(Map<String,String> props, String name) {
		String s = props.get(name);
		return StringUtils.trimToNull(s);
	}
		
    private final BeanUtilsBean bub;
    //private String[] dateFormats = {"dd/MM/yy", "dd/MM/yyyy", "dd/MM/yyyy HH:mm", "dd/MM/yy HH:mm"};
    //private String[] dateFormats = {"dd/MM/yyyy"};

	private final String[] dateFormats = {"dd/MM/yyyy", "dd/MM/yyyy HH:mm", "yyyy/MM/dd HH:mm"};
	
    private static final ThreadLocal<TimeZone> tlTimezone = new ThreadLocal<TimeZone>();

    public static void setTimeZoneForRequest(TimeZone tz) {
        tlTimezone.set(tz);
    }
    
    public static java.util.TimeZone getTimeZoneForRequest() {
        return tlTimezone.get();
    }	
	
    public DataBinder() {
        ConvertUtilsBean2 convertUtilsBean = new ConvertUtilsBean2();
        NullSafeDateTimeConverter dtConverter = new NullSafeDateTimeConverter();
		dtConverter.setPatterns(dateFormats);
        convertUtilsBean.register(dtConverter, Date.class);
        
        BigDecimalConverter bdConverter = new BigDecimalConverter(null);
        convertUtilsBean.register(bdConverter, BigDecimal.class);

        bub = new BeanUtilsBean(convertUtilsBean);
    }

	/**
	 * 
	 * @param bean - the object to bind to, using bean property accessors
	 * @param properties - the map of values to attempt to bind
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException 
	 */
	public void populate(Object bean, Map properties) throws IllegalAccessException, InvocationTargetException {
		populate(bean, properties, null);
	}
	
	/**
	 * 
	 * @param bean
	 * @param properties - the map of properties to attempt to bind to the bean
	 * @param timeZone - timezone to use for date/time conversions
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException 
	 */
    public void populate(Object bean, Map properties,TimeZone timeZone) throws IllegalAccessException, InvocationTargetException {
		tlTimezone.set(timeZone);
		
        // need to cater for check boxes which send no value if not set
        // we use a convention that every check has a hidden input with name=name_checkbox
        // see Formatter.checkbox for details
        List keys = new ArrayList(properties.keySet());
        for(Object key : keys) {
            String k = key.toString();
            if( k.endsWith("_checkbox")) {
                String propName = k.replace(Formatter.CHECKBOX_SUFFIX, "");
                if( !properties.containsKey(propName)) {
                    properties.put(propName, "");
                }
            }
        }
        bub.populate(bean, properties);
    }
}
