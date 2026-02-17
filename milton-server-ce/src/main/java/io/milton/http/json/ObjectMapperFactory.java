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

package io.milton.http.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Factory for ObjectMapper instances with safe default configurations.
 */
public class ObjectMapperFactory {

    private static final ObjectMapper mapper = create();

    /**
     * Returns a thread-safe, shared ObjectMapper instance.
     *
     * @return
     */
    public static ObjectMapper mapper() {
        return mapper;
    }

    /**
     * Creates a new ObjectMapper instance with safe default configurations.
     *
     * @return
     */
    public static ObjectMapper create() {
        ObjectMapper m = new ObjectMapper();
        m.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        m.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        m.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
        return m;
    }
}
