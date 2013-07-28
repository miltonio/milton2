/*
 * Copyright 2012 McEvoy Software Ltd.
 */
package io.milton.http.webdav2;

import io.milton.http.webdav2.SupportedLockValueWriter;
import io.milton.http.LockInfo;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.resource.PropFindableResource;
import io.milton.http.XmlWriter;
import io.milton.http.webdav.SupportedLocks;
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
        Map<String, String> prefixes = new HashMap<String, String>();

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
