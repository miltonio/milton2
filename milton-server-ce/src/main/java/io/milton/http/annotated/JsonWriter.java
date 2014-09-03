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
