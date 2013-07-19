/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
