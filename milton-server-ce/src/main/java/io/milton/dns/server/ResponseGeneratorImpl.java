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
package io.milton.dns.server;

import io.milton.dns.record.Message;
import io.milton.dns.record.RRset;
import io.milton.dns.record.Record;
import io.milton.dns.record.SetResponse;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ResponseGeneratorImpl implements ResponseGenerator {

	@Override
	public byte[] generateReply(Message query, SetResponse sr, Socket s)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
