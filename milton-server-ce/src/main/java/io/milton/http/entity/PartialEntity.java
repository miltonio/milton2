/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
