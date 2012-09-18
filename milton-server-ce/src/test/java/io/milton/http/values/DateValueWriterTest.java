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
