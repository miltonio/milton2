/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.milton.http.values;

import io.milton.http.values.LockTokenValueWriter;
import io.milton.http.LockInfo;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.http.XmlWriter;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class LockTokenValueWriterTest extends TestCase {

	LockTokenValueWriter valueWriter;

	public LockTokenValueWriterTest(String testName) {
		super(testName);
	}

	@Override
	protected void setUp() throws Exception {
		valueWriter = new LockTokenValueWriter();
	}

	public void testSupports() {
		assertTrue(valueWriter.supports(null, null, LockToken.class));
	}

	public void testWriteValue() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XmlWriter xmlWriter = new XmlWriter(out);
		LockInfo lockInfo = new LockInfo(LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.READ, null, LockInfo.LockDepth.ZERO);
		LockTimeout lockTimeout = new LockTimeout(1000l);
		LockToken token = new LockToken("abc123", lockInfo, lockTimeout);
		Map<String,String> prefixes = new HashMap<String, String>();
		
		valueWriter.writeValue(xmlWriter, "ns", "pre", "lock", token, "/test", prefixes);
		
		xmlWriter.flush();
		String xml = out.toString();
		System.out.println(xml);
		System.out.println("---------------------------------");
		
		// Should look like this:
//<D:lockdiscovery>
//<D:activelock>
//<D:locktype><D:read/></D:locktype>
//<D:lockscope><D:exclusive/></D:lockscope>
//<D:depth>Infinity</D:depth>
//<D:owner/><D:timeout>Second-1000</D:timeout>
//<D:locktoken>
//<D:href>opaquelocktoken:abc123</D:href>
//</D:locktoken>
//<D:lockroot>
//<D:href>/test</D:href>
//</D:lockroot>
//</D:activelock>
//</D:lockdiscovery>		
	}

	public void testParse() {
		try {
			valueWriter.parse(null, null, null);
		} catch (UnsupportedOperationException e) {
			// ok
		}
	}
}
