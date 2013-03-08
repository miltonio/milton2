/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.milton.httpclient.calsync.parse;

import io.milton.httpclient.calsync.parse.annotation.*;
import io.milton.httpclient.parse.PropertyAccessor;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author brad
 */
public class CalDavBeanPropertyMapper {

    private final Map<Class, Mapper> mapOfMappers;
    private final PropertyAccessor propertyAccessor;

    public CalDavBeanPropertyMapper(PropertyAccessor propertyAccessor) {
        this.propertyAccessor = propertyAccessor;
        mapOfMappers = new HashMap<Class, Mapper>();
        addMapper(Uid.class, new UidMapper());
        addMapper(Location.class, new LocationMapper());
        addMapper(Summary.class, new SummaryMapper());
        addMapper(Description.class, new DescriptionMapper());
        addMapper(EndDate.class, new EndDateMapper());
        addMapper(StartDate.class, new StartDateMapper());
        addMapper(Timezone.class, new TimezoneMapper());
        addMapper(Organizer.class, new OrganizerMapper());
    }

    private void addMapper(Class c, Mapper m) {
        mapOfMappers.put(c, m);
    }

    public void toBean(Object bean, String icalText) {
        ByteArrayInputStream fin = null;
        try {
            fin = new ByteArrayInputStream(icalText.getBytes("UTF-8"));
            CalendarBuilder builder = new CalendarBuilder();
            net.fortuna.ical4j.model.Calendar cal4jCalendar;
            try {
                cal4jCalendar = builder.build(fin);
            } catch (IOException ex) {
                throw new RuntimeException(icalText, ex);
            } catch (ParserException ex) {
                throw new RuntimeException(icalText, ex);
            }
            PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(bean);
            for (PropertyDescriptor pd : pds) {
                if (pd.getReadMethod() != null && pd.getWriteMethod() != null) {
                    Method read = pd.getReadMethod();
                    Annotation[] annotations = read.getAnnotations();
                    for (Annotation anno : annotations) {
                        Mapper mapper = mapOfMappers.get(anno.annotationType());
                        if (mapper != null) {
                            mapper.mapToBean(cal4jCalendar, bean, pd);
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        } finally {
            IOUtils.closeQuietly(fin);
        }
    }

    /**
     * Find a property with the given annotation and return its value
     *
     * @param bean
     * @param annotationClass
     * @return
     */
    public <T> T getProperty(Object bean, Class annotationClass, Class<T> valueClass) {
        PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(bean);
        for (PropertyDescriptor pd : pds) {
            if (pd.getReadMethod() != null && pd.getWriteMethod() != null) {
                Method read = pd.getReadMethod();
                Annotation[] annotations = read.getAnnotations();
                for (Annotation anno : annotations) {
                    if (anno.annotationType() == annotationClass) {
                        return propertyAccessor.get(bean, read, valueClass);
                    }
                }
            }
        }
        return null;
    }

    public String toVCard(Object bean) {
        net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
        calendar.getProperties().add(new ProdId("-//spliffy.org//iCal4j 1.0//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        VEvent vevent = new VEvent();
        calendar.getComponents().add(vevent);

        PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(bean);
        for (PropertyDescriptor pd : pds) {
            if (pd.getReadMethod() != null && pd.getWriteMethod() != null) {
                Method read = pd.getReadMethod();
                Annotation[] annotations = read.getAnnotations();
                for (Annotation anno : annotations) {
                    Mapper mapper = mapOfMappers.get(anno.annotationType());
                    if (mapper != null) {
                        mapper.mapToCard(calendar, bean, pd);
                    }
                }
            }
        }


        CalendarOutputter outputter = new CalendarOutputter();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            outputter.output(calendar, bout);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (ValidationException ex) {
            throw new RuntimeException(ex);
        }
        return bout.toString();
    }

    private VEvent event(net.fortuna.ical4j.model.Calendar cal) {
        return (VEvent) cal.getComponent("VEVENT");
    }

    private VFreeBusy freeBusy(net.fortuna.ical4j.model.Calendar cal) {
        return (VFreeBusy) cal.getComponent("VFREEBUSY");
    }

    private String getPropValue(Property prop) {
        if (prop == null) {
            return null;
        }
        return prop.getValue();
    }

    private net.fortuna.ical4j.model.Date getDateValue(DateProperty prop) {
        if (prop == null) {
            return null;
        }
        return prop.getDate();
    }

    public abstract class Mapper {

        abstract void mapToBean(net.fortuna.ical4j.model.Calendar calEvent, Object bean, PropertyDescriptor pd);

        abstract void mapToCard(net.fortuna.ical4j.model.Calendar calEvent, Object bean, PropertyDescriptor pd);
    }

    public class UidMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            Property uidProp = null;
            if( vevent != null ) {
                uidProp = vevent.getUid();
            } else {
                VFreeBusy fb = freeBusy(cal);
                uidProp = fb.getUid();
            }
            Method m = pd.getWriteMethod();
            
            String uid = null;
            if (uidProp != null) {
                uid = uidProp.getValue();
            }
            if (uid == null) {
                uid = UUID.randomUUID().toString();
            }
            propertyAccessor.set(bean, m, uid);
        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            String uid = propertyAccessor.get(bean, pd.getReadMethod(), String.class);
            VEvent vevent = event(cal);
            vevent.getProperties().add(new net.fortuna.ical4j.model.property.Uid(uid));
        }
    }

    public class TimezoneMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            Method m = pd.getWriteMethod();
            String tzId = getPropValue(cal.getProperty(Property.TZID));
            propertyAccessor.set(bean, m, tzId);
        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
            String tzId = propertyAccessor.get(bean, pd.getReadMethod(), String.class);
            TimeZone timezone = null;
            if (tzId != null && tzId.length() > 0) {
                timezone = registry.getTimeZone(tzId); // Eg Pacific/Auckland
            }
            // TODO: do we need to use a default time zone if none given?
            if (timezone != null) {
                VTimeZone tz = timezone.getVTimeZone();
                cal.getComponents().add(tz);
            }
        }
    }

    public class LocationMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            if (vevent != null) {
                String s = getPropValue(vevent.getLocation());
                propertyAccessor.set(bean, pd.getWriteMethod(), s);
            }
        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            String s = propertyAccessor.get(bean, pd.getReadMethod(), String.class);
            VEvent vevent = event(cal);
            net.fortuna.ical4j.model.property.Location d = new net.fortuna.ical4j.model.property.Location(s);
            vevent.getProperties().add(d);
        }
    }

    public class OrganizerMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            if (vevent != null) {
                String s = getPropValue(vevent.getOrganizer());
                propertyAccessor.set(bean, pd.getWriteMethod(), s);
            }
        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            String s = propertyAccessor.get(bean, pd.getReadMethod(), String.class);
            if (s == null || s.trim().length() == 0) {
                return;
            }
            VEvent vevent = event(cal);
            net.fortuna.ical4j.model.property.Organizer d;
            try {
                d = new net.fortuna.ical4j.model.property.Organizer(s);
            } catch (URISyntaxException ex) {
                throw new RuntimeException(s, ex);
            }
            vevent.getProperties().add(d);
        }
    }

    public class DescriptionMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            if (vevent != null) {
                String desc = getPropValue(vevent.getDescription());
                propertyAccessor.set(bean, pd.getWriteMethod(), desc);
            } else {
                propertyAccessor.set(bean, pd.getWriteMethod(), null);
            }
        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            String s = propertyAccessor.get(bean, pd.getReadMethod(), String.class);
            if (s == null || s.trim().length() == 0) {
                return;
            }

            VEvent vevent = event(cal);
            if (vevent != null) {
                net.fortuna.ical4j.model.property.Description d = new net.fortuna.ical4j.model.property.Description();
                d.setValue(s);
                vevent.getProperties().add(d);
            }
        }
    }

    public class SummaryMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            if (vevent != null) {
                String s = getPropValue(vevent.getSummary());
                propertyAccessor.set(bean, pd.getWriteMethod(), s);
            }

        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            String s = propertyAccessor.get(bean, pd.getReadMethod(), String.class);
            if (s == null || s.trim().length() == 0) {
                return;
            }

            net.fortuna.ical4j.model.property.Summary d = new net.fortuna.ical4j.model.property.Summary(s);
            vevent.getProperties().add(d);
        }
    }

    public class EndDateMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            Date dt = null;
            if (vevent != null) {
                dt = getDateValue(vevent.getEndDate());
            } else {
                VFreeBusy fb = freeBusy(cal);
                if (fb != null) {
                    dt = getDateValue(fb.getEndDate());
                }
            }
            propertyAccessor.set(bean, pd.getWriteMethod(), dt);

        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            Date d = propertyAccessor.get(bean, pd.getReadMethod(), Date.class);
            if (d == null) {
                return;
            }

            net.fortuna.ical4j.model.Date dt = new net.fortuna.ical4j.model.Date(d);
            net.fortuna.ical4j.model.property.DtEnd p = new net.fortuna.ical4j.model.property.DtEnd(dt);
            vevent.getProperties().add(p);
        }
    }

    public class StartDateMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            Date dt = null;
            if( vevent != null ) {
                dt = getDateValue(vevent.getStartDate());
            } else {
                VFreeBusy fb = freeBusy(cal);
                if (fb != null) {
                    dt = getDateValue(fb.getStartDate());
                }                
            }
            propertyAccessor.set(bean, pd.getWriteMethod(), dt);

        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            Date d = propertyAccessor.get(bean, pd.getReadMethod(), Date.class);
            if (d == null) {
                return;
            }

            net.fortuna.ical4j.model.Date dt = new net.fortuna.ical4j.model.Date(d);
            net.fortuna.ical4j.model.property.DtStart p = new net.fortuna.ical4j.model.property.DtStart(dt);
            vevent.getProperties().add(p);
        }
    }
}
