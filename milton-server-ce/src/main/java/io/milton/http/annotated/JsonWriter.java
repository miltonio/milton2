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

package io.milton.http.annotated;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;

/**
 * Just writes an object to the outputstream in JSON notation.
 * 
 * Normally used with JsonResult
 *
 * @author brad
 */
public class JsonWriter {
    public void write(Object object, OutputStream out) throws IOException {
        JsonConfig cfg = new JsonConfig();
        cfg.setIgnoreTransientFields(true);
        cfg.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);

        JSON json = JSONSerializer.toJSON(object, cfg);
        Writer writer = new PrintWriter(out);
        json.write(writer);
        writer.flush();
    }
}
