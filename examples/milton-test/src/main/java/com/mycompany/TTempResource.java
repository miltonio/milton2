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

package com.mycompany;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.bradmcevoy.http.Range;

/**
 *
 */
public class TTempResource extends TResource{

    public TTempResource(TFolderResource parent, String name) {
        super(parent, name);
    }

    @Override
    protected Object clone(TFolderResource newParent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
        // none
    }

    public String getContentType(String accepts) {
        return "";
    }

}
