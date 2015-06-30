/*
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.http.values;

import io.milton.http.XmlWriter;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class CDataValueWriterTest extends TestCase {
	
	CDataValueWriter writer;
	XmlWriter xmlWriter;
	ByteArrayOutputStream out;
	CData testVal;
	Map<String,String> nsPrefixes;
	
	public CDataValueWriterTest(String testName) {
		super(testName);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		out = new ByteArrayOutputStream();
		writer = new CDataValueWriter();
		xmlWriter = new XmlWriter(out);
	}

	public void testSimple() {
		testVal = new CData("Test cdata value");
		writer.writeValue(xmlWriter, "DAV", "D", "testprop", testVal, "/some/href", nsPrefixes);
		xmlWriter.flush();
		String result = out.toString();		
		System.out.println("output: " + out.toString());
		assertEquals("<D:testprop><![CDATA[Test cdata value]]></D:testprop>", result);
	}	
	
	public void testDoesNotEncodeAmp() {
		testVal = new CData("Test cdata& value");
		writer.writeValue(xmlWriter, "DAV", "D", "testprop", testVal, "/some/href", nsPrefixes);
		xmlWriter.flush();
		String result = out.toString();		
		System.out.println("output: " + out.toString());		
		assertFalse(result.contains("&amp;")); // must not do encoding
	}
	
	public void testDoesEncodeCdataDelimiter() {
		testVal = new CData("]]>");
		writer.writeValue(xmlWriter, "DAV", "D", "testprop", testVal, "/some/href", nsPrefixes);
		xmlWriter.flush();
		String result = out.toString();		
		System.out.println("output: " + out.toString());
		assertEquals("<D:testprop><![CDATA[]]]]><![CDATA[>]]></D:testprop>", result);
	}	
}
