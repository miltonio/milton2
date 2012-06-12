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

/* Licence:
 *   Use this however/wherever you like, just don't blame me if it breaks anything.
 *
 * Credit:
 *   If you're nice, you'll leave this bit:
 *
 *   Class by Pierre-Alexandre Losson -- http://www.telio.be/blog
 *   email : plosson@users.sourceforge.net
 */
package io.milton.http.upload;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import java.io.File;

public class MonitoredDiskFileItemFactory extends DiskFileItemFactory {
    private OutputStreamListener listener = null;
    
    public MonitoredDiskFileItemFactory(OutputStreamListener listener) {
        super();
        this.listener = listener;
    }
    
    public MonitoredDiskFileItemFactory(int sizeThreshold, File repository, OutputStreamListener listener) {
        super(sizeThreshold, repository);
        this.listener = listener;
    }
    
    @Override
    public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName) {
        return new MonitoredDiskFileItem(fieldName, contentType, isFormField, fileName, getSizeThreshold(), getRepository(), listener);
    }
}
