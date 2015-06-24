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

package io.milton.context;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ListFactory implements Factory<List>{

    final String[] ids;
    final List<BeanLocator> beanLocators;

    public ListFactory(String id, List<BeanLocator> beanLocators) {
        ids = new String[] {id};
        this.beanLocators = beanLocators;
    }

    @Override
    public Class[] keyClasses() {
        return null; // only key by id
    }

    @Override
    public String[] keyIds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Registration<List> insert(RootContext context, Context requestContext) {
        List list = new ArrayList();
        for( BeanLocator bl : beanLocators ) {
            Object o = bl.locateBean(requestContext);
            list.add(o);
        }
        Registration<List> reg = new Registration<List>(list, null, context);
        return reg;
    }

    @Override
    public void init(RootContext context) {
        
    }

    @Override
    public void destroy() {
        
    }

    @Override
    public void onRemove(List item) {
        
    }

}
