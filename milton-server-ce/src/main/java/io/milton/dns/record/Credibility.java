/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;


/**
 * Constants relating to the credibility of cached data, which is based on
 * the data's source.  The constants NORMAL and ANY should be used by most
 * callers.
 * @see Cache
 * @see Section
 *
 * @author Brian Wellington
 */

public final class Credibility {

private
Credibility() {}

/** A hint or cache file on disk. */
public static final int HINT			= 0;

/** The additional section of a response. */
public static final int ADDITIONAL		= 1;

/** The additional section of a response. */
public static final int GLUE			= 2;

/** The authority section of a nonauthoritative response. */
public static final int NONAUTH_AUTHORITY	= 3;

/** The answer section of a nonauthoritative response. */
public static final int NONAUTH_ANSWER		= 3;

/** The authority section of an authoritative response. */
public static final int AUTH_AUTHORITY		= 4;

/** The answer section of a authoritative response. */
public static final int AUTH_ANSWER		= 4;

/** A zone. */
public static final int ZONE			= 5;

/** Credible data. */
public static final int NORMAL			= 3;

/** Data not required to be credible. */
public static final int ANY			= 1;

}
