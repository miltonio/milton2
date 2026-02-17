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

package io.milton.http.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.milton.http.FileItem;
import io.milton.http.HttpManager;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.GetableResource;
import io.milton.resource.PostableResource;
import io.milton.resource.Resource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author brad
 */
public class AjaxLoginResource extends JsonResource implements GetableResource, PostableResource {

    public AjaxLoginResource(String name, Resource wrapped) {
        super(wrapped, name, null);
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
        Map<String, Object> map = new HashMap<>();
        Request request = HttpManager.request();
        Boolean loginResult = (Boolean) request.getAttributes().get("loginResult");
        map.put("loginResult", loginResult);
        String userUrl = (String) request.getAttributes().get("userUrl");
        if (userUrl != null) {
            map.put("userUrl", userUrl);
        }
        ObjectMapper mapper = ObjectMapperFactory.mapper();
        mapper.writeValue(out, map);
    }

    @Override
    public Method applicableMethod() {
        return Method.GET;
    }

    @Override
    public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws BadRequestException, NotAuthorizedException, ConflictException {
        return null;
    }

}
