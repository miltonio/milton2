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
 * Model/view abstraction.
 *
 * @author brad
 */
public class ModelAndView {
    private final Map<String, Object> model;

    private final View view;

    /**
     * Creates {@link ModelAndView} with {@link Map} model and {@link View} view.
     * @param model Model.
     * @param view {@link View}.
     */
    public ModelAndView(Map<String, Object> model, View view) {
        this.model = model;
        this.view = view;
    }

    /**
     * Creates {@link ModelAndView} with a template.
     * @param template Template.
     */
    public ModelAndView(String template) {
        view = new View(template);
        this.model = new HashMap<>();
    }

    /**
     * Creates {@link ModelAndView} with a model and template.
     * @param model Model.
     * @param template Template.
     */
    public ModelAndView(Map<String, Object> model, String template) {
        view = new View(template);
        this.model = model;
    }

    /**
     * Creates {@link ModelAndView}.
     * @param modelObjectName Model object name.
     * @param modelObject Model object.
     * @param template Template.
     */
    public ModelAndView(String modelObjectName, Object modelObject, String template) {
        view = new View(template);
        this.model = new HashMap<>();
        model.put(modelObjectName, modelObject);
    }

    /**
     * Returns model.
     * @return {@link Map} model.
     */
    public Map<String, Object> getModel() {
        return model;
    }

    /**
     * Returns View.
     * @return {@link View} view.
     */
    public View getView() {
        return view;
    }


}
