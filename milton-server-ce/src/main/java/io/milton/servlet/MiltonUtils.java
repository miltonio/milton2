/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.milton.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;

public class MiltonUtils {
    public static String stripContext(HttpServletRequest req) {
        String s = req.getRequestURI();        
        String contextPath = req.getContextPath();        
        s = s.replaceFirst( contextPath  , "" );
        return s;
    }
    
    /**
     * 
     * @param context - context to look up mime associations with
     * @param fileName - name of a file to look for mime associations of
     * 
     * @return - a single content type spec
     */
    public static String getContentType(ServletContext context, String fileName) {
        return context.getMimeType(fileName);
    }
}
