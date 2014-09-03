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

import io.milton.http.values.DateValueWriter;
import io.milton.http.XmlWriter;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class DateValueWriterTest extends TestCase {
	
	
	DateValueWriter	dateValueWriter;
	
	@Override
	protected void setUp() throws Exception {
		dateValueWriter = new DateValueWriter();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSupports_NormalDate() {
		assertTrue( dateValueWriter.supports(null, null, Date.class) );
	}
	
	public void testSupports_SqlDate() {
		assertTrue( dateValueWriter.supports(null, null, java.sql.Date.class) );
	}
	
	public void testSupports_SqlTimestamp() {
		assertTrue( dateValueWriter.supports(null, null, Timestamp.class) );
	}	
	
	public void testSupports_FalseForString() {
		assertFalse( dateValueWriter.supports(null, null, String.class) );
	}		


}
