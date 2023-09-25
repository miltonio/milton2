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

import io.milton.annotations.PrincipalSearch;
import io.milton.common.Path;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.principal.PrincipalSearchCriteria;
import io.milton.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * @author brad
 */
public class PrincipalSearchAnnotationHandler extends AbstractAnnotationHandler {

    private static final Logger log = LoggerFactory.getLogger(AnnotationResourceFactory.class);

    public PrincipalSearchAnnotationHandler(final AnnotationResourceFactory outer) {
        super(outer, PrincipalSearch.class);
    }

    public List<? extends Resource> execute(AnnoAddressBookResource addressBookRes, PrincipalSearchCriteria crit) {
        Object source = addressBookRes.getSource();
        try {
            Object value = null;
            ControllerMethod cm = getBestMethod(source.getClass());
            if (cm == null) {
                // look for an annotation on the source itself
                java.lang.reflect.Method m = annoResourceFactory.findMethodForAnno(source.getClass(), annoClass);
                if (m != null) {
                    value = m.invoke(source, crit);
                } else {
                    return null;
                }
            } else {
                value = invoke(cm, addressBookRes, crit);
            }
            if (value != null) {
                return toResourceList(addressBookRes, value);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception executing " + getClass() + " - " + source.getClass(), e);
        }
    }

    private ResourceList toResourceList(AnnoAddressBookResource addressBookRes, Object value) throws NotAuthorizedException, BadRequestException {
        ResourceList list = new ResourceList();
        if (value == null) {
            log.warn("Got null from principal search");
        } else if (list instanceof Collection) {
            Collection col = (Collection) value;
            for (Object o : col) {
                CommonResource cr = toResource(addressBookRes, o);
            }
        }
        return list;
    }

    private CommonResource toResource(AnnoAddressBookResource addressBookRes, Object o) throws NotAuthorizedException, BadRequestException {
        if (o instanceof CommonResource) {
            return (CommonResource) o;
        } else if (o instanceof Resource) {
            Resource r = (Resource) o;
            log.warn("Got an incompatible Resource={} with name={}, should be implementation of CommonResource", o.getClass(), r.getName());
            return null;
        } else if (o instanceof Path) {
            Path path = (Path) o;
            AnnoCollectionResource fromCol;
            if (path.isRelative()) {
                fromCol = addressBookRes;
            } else {
                fromCol = addressBookRes.getRoot();
            }
            Resource r = annoResourceFactory.findFromRoot(fromCol, path);
            return toResource(addressBookRes, r);
        } else if (o instanceof String) {
            // treat as a href
            String s = (String) o;
            Path path = Path.path(s);
            return toResource(addressBookRes, path);
        } else {
            log.warn("Got an incompatible search result with class={}. I dont know how to convert this to a CommonResource. Return a String href or Path (path to resource), or a source object, or a CommonResource", o.getClass());
            return null;
        }

    }
}
