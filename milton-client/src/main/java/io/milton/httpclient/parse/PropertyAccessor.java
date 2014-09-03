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
