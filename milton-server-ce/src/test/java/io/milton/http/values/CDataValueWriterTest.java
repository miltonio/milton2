/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
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
