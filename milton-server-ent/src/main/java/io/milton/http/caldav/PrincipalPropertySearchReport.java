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

package io.milton.http.caldav;

import io.milton.http.report.Report;
import io.milton.resource.Resource;
import org.jdom.Document;

/**
 *
 * @author alex
 */
public class PrincipalPropertySearchReport implements Report {

	@Override
	public String getName() {
		return "principal-property-search";
	}

	@Override
	public String process(String host, String path, Resource r, Document doc) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
