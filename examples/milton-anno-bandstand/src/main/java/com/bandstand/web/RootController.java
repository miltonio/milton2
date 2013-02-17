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
package com.bandstand.web;

import io.milton.annotations.Get;
import io.milton.annotations.ResourceController;
import io.milton.annotations.Root;
import io.milton.common.ModelAndView;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author brad
 */
@ResourceController
public class RootController {
    
    @Root
    public RootController getRoot() {
        return this;
    }
         
    @Get
    public ModelAndView renderHomePage(RootController root) throws UnsupportedEncodingException {
        //return "<html>\n<body><h1>hello world</h1></body></html>".getBytes("UTF-8");        
        return new ModelAndView("controller", this, "homePage"); 
    }
    
    /**
     * Required for the name of the root resource
     * 
     * @return 
     */
    public String getName() {
        return "";
    }
}
