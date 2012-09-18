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

package io.milton.http.values;

import io.milton.http.values.SupportedLockValueWriter;
import io.milton.http.LockInfo;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.resource.PropFindableResource;
import io.milton.http.XmlWriter;
import io.milton.http.webdav.WebDavProtocol.SupportedLocks;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class SupportedLockValueWriterTest extends TestCase {
	
	SupportedLockValueWriter valueWriter;
	
	public SupportedLockValueWriterTest(String testName) {
		super(testName);
	}
	
	@Override
	protected void setUp() throws Exception {
		valueWriter = new SupportedLockValueWriter();
	}


	public void testWriteValue() {
		PropFindableResource res = null;
		SupportedLocks locks = new SupportedLocks(res);
		
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XmlWriter xmlWriter = new XmlWriter(out);
		LockInfo lockInfo = new LockInfo(LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.READ, null, LockInfo.LockDepth.ZERO);
		LockTimeout lockTimeout = new LockTimeout(1000l);
		LockToken token = new LockToken("abc123", lockInfo, lockTimeout);
		Map<String,String> prefixes = new HashMap<String, String>();
		
		valueWriter.writeValue(xmlWriter, "uri", "ns", "aName", locks, "/test", prefixes);
		
		xmlWriter.flush();
		String xml = out.toString();
		System.out.println(xml);
		System.out.println("---------------------------------");		
		
		// Should look like this:
//<D:supportedlock>
//<D:lockentry>
//<D:lockscope><D:exclusive/></D:lockscope>
//<D:locktype><D:write/></D:locktype>
//</D:lockentry>
//</D:supportedlock>
	}

}
