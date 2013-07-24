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

import io.milton.common.LogUtils;
import io.milton.event.EventManager;
import io.milton.event.PropPatchEvent;
import io.milton.http.HttpManager;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.webdav.PropFindResponse;
import io.milton.http.webdav.PropFindResponse.NameAndError;
import io.milton.http.webdav.PropPatchParseResult;
import io.milton.http.webdav.PropPatchSetter;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.property.PropertyAuthoriser;
import io.milton.property.PropertyAuthoriser.CheckResult;
import io.milton.resource.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class JsonPropPatchHandler {

    private static final Logger log = LoggerFactory.getLogger(JsonPropPatchHandler.class);
    private final PropPatchSetter patchSetter;
    private final PropertyAuthoriser permissionService;
    private final EventManager eventManager;

    public JsonPropPatchHandler(PropPatchSetter patchSetter, PropertyAuthoriser permissionService, EventManager eventManager) {
		if( patchSetter == null ) {
			throw new NullPointerException("patchSetter is null");
		}
        this.patchSetter = patchSetter;
        this.permissionService = permissionService;
        this.eventManager = eventManager;
    }

    public PropFindResponse process(Resource wrappedResource, String encodedUrl, Map<String, String> params) throws NotAuthorizedException, ConflictException, BadRequestException {
        log.trace("process");
        Map<QName, String> fields = new HashMap<QName, String>();
        for (String fieldName : params.keySet()) {
            String sFieldValue = params.get(fieldName);
            QName qn;
            if (fieldName.contains(":")) {
                // name is of form uri:local  E.g. MyDav:authorName
                String parts[] = fieldName.split(":");
                String nsUri = parts[0];
                String localName = parts[1];
                qn = new QName(nsUri, localName);
            } else {
                // name is simple form E.g. displayname, default nsUri to DAV
                qn = new QName(WebDavProtocol.NS_DAV.getPrefix(), fieldName);
            }
            log.debug("field: " + qn);
            fields.put(qn, sFieldValue);
        }

        PropPatchParseResult parseResult = new PropPatchParseResult(fields, null);

        if (log.isTraceEnabled()) {
            log.trace("check permissions with: " + permissionService.getClass());
        }
        Set<PropertyAuthoriser.CheckResult> errorFields = permissionService.checkPermissions(HttpManager.request(), Method.PROPPATCH, PropertyAuthoriser.PropertyPermission.WRITE, fields.keySet(), wrappedResource);
        if (errorFields != null && errorFields.size() > 0) {
            log.info("authorisation errors: " + errorFields.size() + " from permissionService: " + permissionService.getClass());
            if (log.isTraceEnabled()) {
                for (CheckResult e : errorFields) {
                    LogUtils.trace(log, " - field error: ", e.getField(), e.getStatus(), e.getDescription());
                }
            }
            throw new NotAuthorizedException(wrappedResource);
        } else {
            LogUtils.trace(log, "setting properties with", patchSetter.getClass());
            PropFindResponse resp = patchSetter.setProperties(encodedUrl, parseResult, wrappedResource);
            if (eventManager != null) {
                log.trace("fire event");
                eventManager.fireEvent(new PropPatchEvent(wrappedResource, resp));
            } else {
                log.trace("no event manager");
            }
            if (resp.getErrorProperties().size() > 0) {
                LogUtils.warn(log, "Encountered errors setting fields with patch setter", patchSetter.getClass());
            }
            if (log.isTraceEnabled()) {
                if (resp.getErrorProperties().size() > 0) {
                    for (List<NameAndError> e : resp.getErrorProperties().values()) {
                        for (NameAndError ne : e) {
                            LogUtils.trace(log, " - field error setting properties: ", ne.getName(), ne.getError());
                        }
                    }
                }
            }
            return resp;

        }
    }

    public PropertyAuthoriser getPermissionService() {
        return permissionService;
    }
}
