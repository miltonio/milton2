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

package io.milton.http;

import java.io.OutputStream;

/**
 * Represents an item which has been uploaded in a form POST
 * 
 * @author brad
 */
public interface FileItem {

    String getContentType();

    /**
     * The name of the field which declared the file control
     * @return
     */
    String getFieldName();

    /**
     * To read the uploaded file
     *
     * @return
     */
    java.io.InputStream getInputStream();

    /**
     * The name of the uploaded file
     * @return
     */
    java.lang.String getName();

    /**
     * The size of the uploaded file
     * @return
     */
    long getSize();

    /**
     * To allow writing to the uploaded file. Not always supported
     * @return
     */
    OutputStream getOutputStream();
}
