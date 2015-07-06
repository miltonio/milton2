/*
 *
 * Copyright 2014 McEvoy Software Ltd.
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
package io.milton.http.annotated;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author brad
 */
public class ChildrenOfTypeMap implements Map<String,ResourceList>{

    private final ResourceList list;

    public ChildrenOfTypeMap(ResourceList list) {
        this.list = list;
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return true;
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ResourceList get(Object key) {
        return list.ofType(key.toString());               
    }

    @Override
    public ResourceList put(String key, ResourceList value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ResourceList remove(Object key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putAll(Map<? extends String, ? extends ResourceList> m) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<ResourceList> values() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Entry<String, ResourceList>> entrySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
