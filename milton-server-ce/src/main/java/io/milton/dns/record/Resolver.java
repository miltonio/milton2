/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;


import java.io.*;
import java.util.*;

/**
 * Interface describing a resolver.
 *
 * @author Brian Wellington
 */

public interface Resolver {

/**
 * Sets the port to communicate with on the server
 * @param port The port to send messages to
 */
void setPort(int port);

/**
 * Sets whether TCP connections will be sent by default
 * @param flag Indicates whether TCP connections are made
 */
void setTCP(boolean flag);

/**
 * Sets whether truncated responses will be ignored.  If not, a truncated
 * response over UDP will cause a retransmission over TCP.
 * @param flag Indicates whether truncated responses should be ignored.
 */
void setIgnoreTruncation(boolean flag);

/**
 * Sets the EDNS version used on outgoing messages.
 * @param level The EDNS level to use.  0 indicates EDNS0 and -1 indicates no
 * EDNS.
 * @throws IllegalArgumentException An invalid level was indicated.
 */
void setEDNS(int level);

/**
 * Sets the EDNS information on outgoing messages.
 * @param level The EDNS level to use.  0 indicates EDNS0 and -1 indicates no
 * EDNS.
 * @param payloadSize The maximum DNS packet size that this host is capable
 * of receiving over UDP.  If 0 is specified, the default (1280) is used.
 * @param flags EDNS extended flags to be set in the OPT record.
 * @param options EDNS options to be set in the OPT record, specified as a
 * List of OPTRecord.Option elements.
 * @throws IllegalArgumentException An invalid field was specified.
 * @see OPTRecord
 */
void setEDNS(int level, int payloadSize, int flags, List options);

/**
 * Specifies the TSIG key that messages will be signed with
 * @param key The key
 */
void setTSIGKey(TSIG key);

/**
 * Sets the amount of time to wait for a response before giving up.
 * @param secs The number of seconds to wait.
 * @param msecs The number of milliseconds to wait.
 */
void setTimeout(int secs, int msecs);

/**
 * Sets the amount of time to wait for a response before giving up.
 * @param secs The number of seconds to wait.
 */
void setTimeout(int secs);

/**
 * Sends a message and waits for a response.
 * @param query The query to send.
 * @return The response
 * @throws IOException An error occurred while sending or receiving.
 */
Message send(Message query) throws IOException;

/**
 * Asynchronously sends a message registering a listener to receive a callback
 * on success or exception.  Multiple asynchronous lookups can be performed
 * in parallel.  Since the callback may be invoked before the function returns,
 * external synchronization is necessary.
 * @param query The query to send
 * @param listener The object containing the callbacks.
 * @return An identifier, which is also a parameter in the callback
 */
Object sendAsync(final Message query, final ResolverListener listener);

}
