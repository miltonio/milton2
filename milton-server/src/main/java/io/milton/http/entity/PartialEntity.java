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

package io.milton.http.entity;

import io.milton.common.RangeUtils;
import io.milton.http.Range;
import io.milton.http.Response;
import io.milton.common.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.List;

public class PartialEntity implements Response.Entity {

    private static final Logger log = LoggerFactory.getLogger(PartialEntity.class);

    private List<Range> ranges;
    private File temp;

    public PartialEntity(List<Range> ranges, File temp) {
        this.ranges = ranges;
        this.temp = temp;
    }

    public List<Range> getRanges() {
        return ranges;
    }

    public File getTemp() {
        return temp;
    }

    @Override
    public void write(Response response, OutputStream outputStream) throws Exception {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(temp);
            RangeUtils.writeRanges(fin, ranges, outputStream);
        } finally {
            StreamUtils.close(fin);
        }
    }
}
