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

package io.milton.http.http11;

import io.milton.http.http11.Http11ResponseHandler;
import io.milton.http.http11.GetHandler;
import io.milton.http.HandlerHelper;
import junit.framework.TestCase;

import static org.easymock.classextension.EasyMock.*;

/**
 *
 * @author brad
 */
public class GetHandlerTest extends TestCase {
	
	GetHandler getHandler;
	Http11ResponseHandler responseHandler;
	HandlerHelper handlerHelper;
	
	public GetHandlerTest(String testName) {
		super(testName);
	}
	
	@Override
	protected void setUp() throws Exception {
		responseHandler = createMock(Http11ResponseHandler.class);
		handlerHelper = createMock(HandlerHelper.class);
		getHandler = new GetHandler(responseHandler, handlerHelper);
	}

	public void testProcess() throws Exception {
	}

	public void testProcessResource() throws Exception {
	}

	public void testProcessExistingResource() throws Exception {
	}


	public void testGetMethods() {
	}

	public void testIsCompatible() {
	}
}
