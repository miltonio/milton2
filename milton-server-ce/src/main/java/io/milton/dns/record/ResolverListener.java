/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;


import java.util.EventListener;

/**
 * An interface to the asynchronous resolver.
 * @see Resolver
 *
 * @author Brian Wellington
 */

public interface ResolverListener extends EventListener {

/**
 * The callback used by an asynchronous resolver
 * @param id The identifier returned by Resolver.sendAsync()
 * @param m The response message as returned by the Resolver
 */
void receiveMessage(Object id, Message m);

/**
 * The callback used by an asynchronous resolver when an exception is thrown
 * @param id The identifier returned by Resolver.sendAsync()
 * @param e The thrown exception
 */
void handleException(Object id, Exception e);

}
