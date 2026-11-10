/*
 * Copyright 2012 McEvoy Software Ltd.
 */
package io.milton.http.webdav2;

import io.milton.http.LockInfo;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.resource.PropFindableResource;
import io.milton.http.XmlWriter;
import io.milton.http.webdav.SupportedLocks;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author brad
 */
public class SupportedLockValueWriterTest {

    SupportedLockValueWriter valueWriter;


    @BeforeEach
    protected void setUp() throws Exception {
        valueWriter = new SupportedLockValueWriter();
    }

    @Test
    public void testWriteValue() {
        PropFindableResource res = null;
        SupportedLocks locks = new SupportedLocks(res);


        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlWriter xmlWriter = new XmlWriter(out);
        LockInfo lockInfo = new LockInfo(LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.READ, null, LockInfo.LockDepth.ZERO);
        LockTimeout lockTimeout = new LockTimeout(1000L);
        LockToken token = new LockToken("abc123", lockInfo, lockTimeout);
        Map<String, String> prefixes = new HashMap<>();

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
