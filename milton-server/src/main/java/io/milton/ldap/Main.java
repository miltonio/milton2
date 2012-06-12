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

package io.milton.ldap;

import io.milton.http.HandlerHelper;
import io.milton.http.webdav.DefaultWebDavResponseHandler;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.property.BeanPropertySource;
import io.milton.property.PropertySource;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class Main {
	public static void main(String[] args) throws Exception {
		System.out.println("Starting milton ldap...");
		MemoryUserSessionFactory factory = new MemoryUserSessionFactory();
		List<PropertySource> propertySources = new ArrayList<PropertySource>();
		BeanPropertySource ps = new BeanPropertySource();
		System.out.println("Using bean property source: " + ps);
		propertySources.add( ps);
		propertySources.add( new WebDavProtocol(new DefaultWebDavResponseHandler(null), new HandlerHelper(null)));
		
		// TODO: add property sources
		
		factory.addUser("userA", "password", "joe", "bloggs", "joeblogss@blogs.com");
		factory.addUser("userB", "password", "joe2", "bloggs2", "joeblogss2@blogs.com");
		factory.addUser("userC", "password", "joe3", "bloggs3", "joeblogss3@blogs.com");
                NullLdapTransactionManager transactionManager = new NullLdapTransactionManager();
		LdapServer ldapServer = new LdapServer(transactionManager, factory, propertySources, 8389, true, "localhost");				
		ldapServer.start();
		System.out.println("Started");
		while(true) {
			Thread.sleep(5000);
			System.out.println("still running...");
		}
	}
			
}
