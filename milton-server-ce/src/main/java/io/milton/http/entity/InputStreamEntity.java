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

import io.milton.http.Response;
import io.milton.common.ReadingException;
import io.milton.common.StreamUtils;
import io.milton.common.WritingException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;

public class InputStreamEntity implements Response.Entity {

    private static final Logger log = LoggerFactory.getLogger(InputStreamEntity.class);

    private InputStream inputStream;

    public InputStreamEntity(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public void write(Response response, OutputStream outputStream) throws Exception {
        try {
            StreamUtils.readTo(inputStream, outputStream);
        } catch (ReadingException ex) {
            throw new RuntimeException(ex);
        } catch (WritingException ex) {
            log.warn("exception writing, client probably closed connection", ex);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        log.trace("finished sending content");
    }
}
