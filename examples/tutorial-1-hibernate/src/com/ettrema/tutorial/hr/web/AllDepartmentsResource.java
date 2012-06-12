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

package com.ettrema.tutorial.hr.web;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Request.Method;

public class AllDepartmentsResource implements PropFindableResource, CollectionResource{

	private final HrResourceFactory resourceFactory;
	private final Session session;
	
	public AllDepartmentsResource(HrResourceFactory resourceFactory, Session session) {
		this.resourceFactory = resourceFactory;
		this.session = session;
	}	
	
	@Override
	public Date getCreateDate() {
		// Unknown
		return null;
	}

	@Override
	public Object authenticate(String user, String pwd) {
		// always allow
		return user;
	}

	@Override
	public boolean authorise(Request arg0, Method arg1, Auth arg2) {
		// Always allow
		return true;
	}

	@Override
	public String checkRedirect(Request arg0) {
		// No redirects
		return null;
	}

	@Override
	public Date getModifiedDate() {
		// Unknown
		return null;
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public String getRealm() {
		return HrResourceFactory.REALM;
	}

	@Override
	public String getUniqueId() {
		return null;
	}

	@Override
	public Resource child(String name) {
		return null;
	}

	@Override
	public List<? extends Resource> getChildren() {
		return resourceFactory.findAllDepartments(session);
	}
}
