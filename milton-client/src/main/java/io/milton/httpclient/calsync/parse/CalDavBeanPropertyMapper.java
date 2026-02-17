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

import io.milton.httpclient.PropertyUtils;
import io.milton.httpclient.calsync.parse.annotation.*;
import io.milton.httpclient.parse.PropertyAccessor;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;
import net.fortuna.ical4j.validate.ValidationException;
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
        mapOfMappers = new HashMap<>();
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
            } catch (IOException | ParserException ex) {
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
        calendar.add(new ProdId("-//spliffy.org//iCal4j 1.0//EN"));
        calendar.add(ImmutableVersion.VERSION_2_0);
        VEvent vevent = new VEvent();
        calendar.add(vevent);

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
        } catch (IOException | ValidationException ex) {
            throw new RuntimeException(ex);
        }
        return bout.toString();
    }

    private VEvent event(net.fortuna.ical4j.model.Calendar cal) {
        return (VEvent) cal.getComponent("VEVENT").orElse(null);
    }

    private Optional<VFreeBusy> freeBusy(net.fortuna.ical4j.model.Calendar cal) {
        return cal.getComponent("VFREEBUSY");
    }

    private String getPropValue(Property prop) {
        if (prop == null) {
            return null;
        }
        return prop.getValue();
    }

    public abstract static class Mapper {

        abstract void mapToBean(net.fortuna.ical4j.model.Calendar calEvent, Object bean, PropertyDescriptor pd);

        abstract void mapToCard(net.fortuna.ical4j.model.Calendar calEvent, Object bean, PropertyDescriptor pd);
    }

    public class UidMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            final AtomicReference<String> uidProp = new AtomicReference<>();
            if( vevent != null ) {
                vevent.getUid().ifPresent(uuid -> uidProp.set(uuid.getValue()));
            } else {
                freeBusy(cal).flatMap(Component::getUid).ifPresent(uuid -> uidProp.set(uuid.getValue()));
            }
            Method m = pd.getWriteMethod();

            String uid = null;
            if (uidProp.get() != null) {
                uid = uidProp.get();
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
            if (vevent != null) {
                vevent.add(new net.fortuna.ical4j.model.property.Uid(uid));
            }
        }
    }

    public class TimezoneMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            Method m = pd.getWriteMethod();
            String tzId = getPropValue(cal.getProperty(Property.TZID).orElse(null));
            propertyAccessor.set(bean, m, tzId);
        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
            String tzId = propertyAccessor.get(bean, pd.getReadMethod(), String.class);
            TimeZone timezone = null;
            if (tzId != null && !tzId.isEmpty()) {
                timezone = registry.getTimeZone(tzId); // Eg Pacific/Auckland
            }
            // TODO: do we need to use a default time zone if none given?
            if (timezone != null) {
                VTimeZone tz = timezone.getVTimeZone();
                cal.add(tz);
            }
        }
    }

    public class LocationMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            if (vevent != null) {
                String s = getPropValue(vevent.getLocation().orElse(null));
                propertyAccessor.set(bean, pd.getWriteMethod(), s);
            }
        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            String s = propertyAccessor.get(bean, pd.getReadMethod(), String.class);
            VEvent vevent = event(cal);
            net.fortuna.ical4j.model.property.Location d = new net.fortuna.ical4j.model.property.Location(s);
            if (vevent != null) {
                vevent.add(d);
            }
        }
    }

    public class OrganizerMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            if (vevent != null) {
                String s = getPropValue(vevent.getOrganizer().orElse(null));
                propertyAccessor.set(bean, pd.getWriteMethod(), s);
            }
        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            String s = propertyAccessor.get(bean, pd.getReadMethod(), String.class);
            if (s == null || s.trim().isEmpty()) {
                return;
            }
            VEvent vevent = event(cal);
            if (vevent != null) {
                net.fortuna.ical4j.model.property.Organizer d;
                d = new net.fortuna.ical4j.model.property.Organizer(s);
                vevent.add(d);
            }
        }
    }

    public class DescriptionMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            if (vevent != null) {
                String desc = getPropValue(vevent.getDescription().orElse(null));
                propertyAccessor.set(bean, pd.getWriteMethod(), desc);
            } else {
                propertyAccessor.set(bean, pd.getWriteMethod(), null);
            }
        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            String s = propertyAccessor.get(bean, pd.getReadMethod(), String.class);
            if (s == null || s.trim().isEmpty()) {
                return;
            }

            VEvent vevent = event(cal);
            if (vevent != null) {
                net.fortuna.ical4j.model.property.Description d = new net.fortuna.ical4j.model.property.Description();
                d.setValue(s);
                vevent.add(d);
            }
        }
    }

    public class SummaryMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            if (vevent != null) {
                String s = getPropValue(vevent.getSummary().orElse(null));
                propertyAccessor.set(bean, pd.getWriteMethod(), s);
            }

        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            if (vevent != null) {
                String s = propertyAccessor.get(bean, pd.getReadMethod(), String.class);
                if (s == null || s.trim().isEmpty()) {
                    return;
                }

                net.fortuna.ical4j.model.property.Summary d = new net.fortuna.ical4j.model.property.Summary(s);
                vevent.add(d);
            }
        }
    }

    public class EndDateMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            final AtomicReference<LocalDateTime> dt = new AtomicReference<>();
            if (vevent != null) {
                vevent.getDateTimeEnd().ifPresent(ed -> dt.set(LocalDateTime.from(ed.getDate())));
            } else {
                freeBusy(cal).flatMap(DateTimePropertyAccessor::getDateTimeEnd).ifPresent(ed -> {
                    dt.set(LocalDateTime.from(ed.getDate()));
                });
            }
            propertyAccessor.set(bean, pd.getWriteMethod(), dt.get());
        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            if (vevent != null) {
                Date d = propertyAccessor.get(bean, pd.getReadMethod(), Date.class);
                if (d == null) {
                    return;
                }

                net.fortuna.ical4j.model.property.DtEnd<LocalDateTime> p =
                        new net.fortuna.ical4j.model.property.DtEnd<>(d.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime());
                vevent.add(p);
            }
        }
    }

    public class StartDateMapper extends Mapper {

        @Override
        void mapToBean(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            final AtomicReference<LocalDateTime> dt = new AtomicReference<>();
            if (vevent != null) {
                vevent.getDateTimeStart().ifPresent(ed -> dt.set(LocalDateTime.from(ed.getDate())));
            } else {
                freeBusy(cal).flatMap(DateTimePropertyAccessor::getDateTimeStart).ifPresent(ed -> {
                    dt.set(LocalDateTime.from(ed.getDate()));
                });
            }
            propertyAccessor.set(bean, pd.getWriteMethod(), dt.get());

        }

        @Override
        void mapToCard(net.fortuna.ical4j.model.Calendar cal, Object bean, PropertyDescriptor pd) {
            VEvent vevent = event(cal);
            if (vevent != null) {
                Date d = propertyAccessor.get(bean, pd.getReadMethod(), Date.class);
                if (d == null) {
                    return;
                }

                net.fortuna.ical4j.model.property.DtEnd<LocalDateTime> p =
                        new net.fortuna.ical4j.model.property.DtEnd<>(d.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime());
                vevent.add(p);
            }
        }
    }
}
