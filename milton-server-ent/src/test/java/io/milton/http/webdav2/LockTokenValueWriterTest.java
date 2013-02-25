/*
 * Copyright 2012 McEvoy Software Ltd.
 */

package io.milton.http.webdav2;

import io.milton.http.webdav2.LockTokenValueWriter;
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
