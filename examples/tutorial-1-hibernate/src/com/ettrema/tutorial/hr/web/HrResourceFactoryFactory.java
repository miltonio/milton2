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

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.AuthenticationService;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.ResourceFactoryFactory;
import com.bradmcevoy.http.webdav.DefaultWebDavResponseHandler;
import com.bradmcevoy.http.webdav.WebDavResponseHandler;
import com.ettrema.tutorial.hr.domain.Department;


public class HrResourceFactoryFactory implements ResourceFactoryFactory {

	private Logger log = LoggerFactory.getLogger(HrResourceFactoryFactory.class);
	
	private static SessionFactory sessionFactory;
	private static AuthenticationService authenticationService;
	private static HrResourceFactory resourceFactory;

	@Override
	public ResourceFactory createResourceFactory() {
		return resourceFactory;
	}

	@Override
	public WebDavResponseHandler createResponseHandler() {
		return new DefaultWebDavResponseHandler(authenticationService);
	}

	@Override
	public void init() {
		log.debug("init");
		if( authenticationService == null ) {
			authenticationService = new AuthenticationService(); 
			sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
			resourceFactory = new HrResourceFactory(sessionFactory);			
			checkInitialData();
		}
	}
	
	private void checkInitialData() {
		Session session = sessionFactory.openSession();
		
		List existingDepartments = session.createCriteria(Department.class).list();
		if( existingDepartments == null || existingDepartments.size() == 0) {
			log.debug("creating initial data");
			Transaction tx = session.beginTransaction();
			Department d = Department.create("Information Technology");
			session.save(d);
			d = Department.create("Finance");
			session.save(d);
			d = Department.create("Human Resources");
			session.save(d);
			tx.commit();
			session.close();
		} else {
			log.debug("database already has department data: " + existingDepartments.size());
		}
	}

}
