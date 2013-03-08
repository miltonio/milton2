/*
 * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)
 * 
 * Copied from the DnsJava project
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
package io.milton.dns;

import java.io.*;

/**
 * An exception thrown when unable to parse text.
 *
 * @author Brian Wellington
 */
public class TextParseException extends IOException {

    public TextParseException() {
        super();
    }

    public TextParseException(String s) {
        super(s);
    }
}
