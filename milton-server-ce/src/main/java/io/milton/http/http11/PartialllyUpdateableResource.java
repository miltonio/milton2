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

package io.milton.http.http11;

import io.milton.http.Range;
import io.milton.resource.ReplaceableResource;
import java.io.InputStream;

/**
 * A resource which, as well as being completely replaceable, can have its content
 * partially replaced. ie individual ranges can be set
 *
 * While PutHandler will do this for you even if you don't implement this interface,
 * the approach used might not be efficient. Ie milton will retrieve your complete
 * content, then insert the update, then set the entire content back again like
 * a regular put.
 *
 * By implementing this interface you have control over how you manage the
 * updated resource.
 *
 *
 * @author brad
 */
public interface PartialllyUpdateableResource extends ReplaceableResource {
    /**
     * Update the content with the date in the given inputstream, affecting
     * only those bytes in the given range.
     *
     * Note that the range positions are zero-based, so the first byte is 0
     *
     * @param range - the range to update
     * @param in - the inputstream containing the data
     */
    void replacePartialContent(Range range, InputStream in);
}
