/*
 * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)
 * 
 * Copied from the DnsJava project
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
package io.milton.dns.spi;

import java.lang.reflect.Proxy;

import sun.net.spi.nameservice.*;

/**
 * The descriptor class for the dnsjava name service provider.
 *
 * @author Brian Wellington
 * @author Paul Cowan (pwc21@yahoo.com)
 */
public class DNSJavaNameServiceDescriptor implements NameServiceDescriptor {

	private static NameService nameService;

	static {
		ClassLoader loader = NameService.class.getClassLoader();
		nameService = (NameService) Proxy.newProxyInstance(loader,
				new Class[]{NameService.class},
				new DNSJavaNameService());
	}

	/**
	 * Returns a reference to a dnsjava name server provider.
	 */
	public NameService createNameService() {
		return nameService;
	}

	public String getType() {
		return "dns";
	}

	public String getProviderName() {
		return "dnsjava";
	}
}
