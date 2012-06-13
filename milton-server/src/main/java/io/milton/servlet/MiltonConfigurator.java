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

import io.milton.http.HttpManager;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * Implement this to customise how the HttpManager is created in the servlet
 *
 * @author brad
 */
public interface MiltonConfigurator {
    HttpManager configure(ServletConfig config) throws ServletException;

    /**
     * called on destroy from the servlet
     */
    void shutdown();
}
