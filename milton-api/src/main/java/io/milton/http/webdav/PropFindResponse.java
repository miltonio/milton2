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

package io.milton.http.webdav;

import io.milton.http.Response;
import io.milton.http.Response.Status;
import io.milton.common.Utils;
import io.milton.http.values.ValueAndType;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

public class PropFindResponse {

    private final String href;
    private Map<QName, ValueAndType> knownProperties;
    private Map<Response.Status, List<NameAndError>> errorProperties;

    public PropFindResponse(String href, Map<QName, ValueAndType> knownProperties, Map<Response.Status, List<NameAndError>> errorProperties) {
        super();
        this.href = Utils.stripServer(href);
        this.knownProperties = knownProperties;
        this.errorProperties = errorProperties;
    }

    public String getHref() {
        return href;
    }

    public Map<QName, ValueAndType> getKnownProperties() {
        return knownProperties;
    }

    public Map<Status, List<NameAndError>> getErrorProperties() {
        return errorProperties;
    }
 
    /**
     * Carries the qualified name of a field in error, and an optional attribute
     * with textual information describing the error.
     *
     * This might be a validation error, for example
     *
     */
    public static class NameAndError {

        private final QName name;
        private final String error;

        public NameAndError(QName name, String error) {
            this.name = name;
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public QName getName() {
            return name;
        }
    }
}
