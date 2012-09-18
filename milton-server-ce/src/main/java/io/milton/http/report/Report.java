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

package io.milton.http.report;

import io.milton.resource.Resource;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;

/**
 * Represents a known report type, is delegated to by the ReportHandler
 *
 * @author brad
 */
public interface Report {
    /**
     * The name of the report, as used in REPORT requests
     * 
     * @return
     */
    String getName();

    /**
     * Process the requested report body, and return a document containing the
     * response body.
     *
     * Must be a multistatus response.
     *
     * @param host 
     * @param r 
     * @param doc
     * @return the response body, usually xml
     */
    String process(String host, String path, Resource r, org.jdom.Document doc) throws BadRequestException, ConflictException, NotAuthorizedException;
}
