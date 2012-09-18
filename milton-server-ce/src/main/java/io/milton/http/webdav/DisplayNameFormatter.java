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

import io.milton.resource.PropFindableResource;

/**
 * This interface serves to allow users of milton to implement different
 * display name strategies.
 *
 * The display name is completely arbitrary, ie it is not in any way necessarily
 * related to the actual name used to contruct the href.
 *
 * This class also serves as a mechanism for deciding whether to wrap the display
 * name in a CDATA element.
 *
 * @author brad
 */
public interface DisplayNameFormatter {
    /**
     * Generate the exact text to appear inside display name elements. No
     * further encoding of this text is applied when generating the xml.
     *
     * @param res
     * @return
     */
    String formatDisplayName(PropFindableResource res);
}
