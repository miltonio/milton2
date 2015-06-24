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
package io.milton.common;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author brad
 */
public class ModelAndView {
    private Map<String,Object> model;
    
    private View view;

    public ModelAndView(Map<String, Object> model, View view) {
        this.model = model;
        this.view = view;
    }

    public ModelAndView(String template) {
        view = new View(template);
        this.model = new HashMap<String, Object>();
    }    
    
    public ModelAndView(Map<String, Object> model, String template) {
        view = new View(template);
        this.model = model;
    }

    public ModelAndView(String modelObjectName, Object modelObject, String template) {
        view = new View(template);
        this.model = new HashMap<String, Object>();
        model.put(modelObjectName, modelObject);
    }
    
    
    public Map<String, Object> getModel() {
        return model;
    }

    public View getView() {
        return view;
    }
    
    
    
}
