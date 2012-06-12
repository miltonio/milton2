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
package io.milton.httpclient.parse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class PropertyAccessor {
    
    private final List<Converter> converters;

    public PropertyAccessor(List<Converter> converters) {
        this.converters = converters;
    }

    public PropertyAccessor() {
        converters = new ArrayList<Converter>();
        converters.add(new UUIDConverter());
    }

    
    
    
    public void set(Object bean, Method writeMethod, Object value) {
        Class<?>[] paramTypes = writeMethod.getParameterTypes();
        if( paramTypes == null || paramTypes.length == 0 ) {
            throw new RuntimeException("Can't set no-args setter: " + writeMethod.getName() + " on class " + bean.getClass());
        }
        if( paramTypes.length > 1 ) {
            throw new RuntimeException("Cant set multi arg setter: " + writeMethod.getName() + " on class: " + bean.getClass());
        }
        
        try {
            if (value == null) {
                writeMethod.invoke(bean, (Object) null);
            } else {
                Class source = value.getClass();
                Class dest = paramTypes[0];
                if (!dest.isAssignableFrom(source)) {
                    value = convert(value, dest);
                }
                writeMethod.invoke(bean, value);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Prop: " + writeMethod.getName(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Prop: " + writeMethod.getName(), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Prop: " + writeMethod.getName(), e);
        }
    }
    
    public <T> T get(Object bean, Method readMethod, Class<T> c) {
        Class<?>[] paramTypes = readMethod.getParameterTypes();
        if( paramTypes != null && paramTypes.length > 0 ) {
            throw new RuntimeException("Can't get with multi args getter: " + readMethod.getName() + " on class " + bean.getClass());
        }
        Object value;
        try {
            value = readMethod.invoke(bean);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Getter: " + readMethod.getName(), ex);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Getter: " + readMethod.getName(), ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException("Getter: " + readMethod.getName(), ex);
        }
        if( value == null ) {
            return null;
        } else {
            Class rawClass = value.getClass();
            if( !c.isAssignableFrom(rawClass)) {
                value = convert(value, c);
            }
        }
        return (T) value;
    }
    
    private Object convert(Object value, Class dest) {
        for( Converter c : converters) {
            if( c.getTarget().isAssignableFrom(dest)) {
                return c.convert(value);
            }
        }
        throw new RuntimeException("No converters are compatible with the dest class: " + dest.getCanonicalName());
    }
    
    public interface Converter {
        Class getTarget();
        Object convert(Object source);
    }
    
    public class UUIDConverter implements Converter {

        @Override
        public Class getTarget() {
            return UUID.class;
        }

        @Override
        public Object convert(Object source) {
            if( source instanceof UUID) {
                return source;
            } else if( source instanceof String) {
                String s = (String) source;
                return UUID.fromString(s);
            } else {
                throw new RuntimeException("Unsupported source type: " + source.getClass());
            }
        }        
    }
}
