/*
 * Copied from the DnsJava project
 *
 * Copyright (c) 1998-2011, Brian Wellington.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package io.milton.dns.spi;

import io.milton.dns.Name;
import io.milton.dns.TextParseException;
import io.milton.dns.record.AAAARecord;
import io.milton.dns.record.ARecord;
import io.milton.dns.record.ExtendedResolver;
import io.milton.dns.record.Lookup;
import io.milton.dns.record.PTRRecord;
import io.milton.dns.record.Record;
import io.milton.dns.record.Resolver;
import io.milton.dns.record.ReverseMap;
import io.milton.dns.record.Type;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

/**
 * This class implements a Name Service Provider, which Java can use (starting
 * with version 1.4), to perform DNS resolutions instead of using the standard
 * calls. <p> This Name Service Provider uses dnsjava. <p> To use this provider,
 * you must set the following system property:
 * <b>sun.net.spi.nameservice.provider.1=dns,dnsjava</b>
 *
 * @author Brian Wellington
 * @author Paul Cowan (pwc21@yahoo.com)
 */
public class DNSJavaNameService implements InvocationHandler {

	private static final String nsProperty = "sun.net.spi.nameservice.nameservers";
	private static final String domainProperty = "sun.net.spi.nameservice.domain";
	private static final String v6Property = "java.net.preferIPv6Addresses";
	private boolean preferV6 = false;

	/**
	 * Creates a DNSJavaNameService instance. <p> Uses the
	 * <b>sun.net.spi.nameservice.nameservers</b>,
	 * <b>sun.net.spi.nameservice.domain</b>, and
	 * <b>java.net.preferIPv6Addresses</b> properties for configuration.
	 */
	protected DNSJavaNameService() {
		String nameServers = System.getProperty(nsProperty);
		String domain = System.getProperty(domainProperty);
		String v6 = System.getProperty(v6Property);

		if (nameServers != null) {
			StringTokenizer st = new StringTokenizer(nameServers, ",");
			String[] servers = new String[st.countTokens()];
			int n = 0;
			while (st.hasMoreTokens()) {
				servers[n++] = st.nextToken();
			}
			try {
				Resolver res = new ExtendedResolver(servers);
				Lookup.setDefaultResolver(res);
			} catch (UnknownHostException e) {
				System.err.println("DNSJavaNameService: invalid "
						+ nsProperty);
			}
		}

		if (domain != null) {
			try {
				Lookup.setDefaultSearchPath(new String[]{domain});
			} catch (TextParseException e) {
				System.err.println("DNSJavaNameService: invalid "
						+ domainProperty);
			}
		}

		if (v6 != null && v6.equalsIgnoreCase("true")) {
			preferV6 = true;
		}
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			if (method.getName().equals("getHostByAddr")) {
				return this.getHostByAddr((byte[]) args[0]);
			} else if (method.getName().equals("lookupAllHostAddr")) {
				InetAddress[] addresses;
				addresses = this.lookupAllHostAddr((String) args[0]);
				Class returnType = method.getReturnType();
				if (returnType.equals(InetAddress[].class)) {
					// method for Java >= 1.6
					return addresses;
				} else if (returnType.equals(byte[][].class)) {
					// method for Java <= 1.5
					int naddrs = addresses.length;
					byte[][] byteAddresses = new byte[naddrs][];
					byte[] addr;
					for (int i = 0; i < naddrs; i++) {
						addr = addresses[i].getAddress();
						byteAddresses[i] = addr;
					}
					return byteAddresses;
				}
			}
		} catch (Throwable e) {
			System.err.println("DNSJavaNameService: Unexpected error.");
			e.printStackTrace();
			throw e;
		}
		throw new IllegalArgumentException(
				"Unknown function name or arguments.");
	}

	/**
	 * Performs a forward DNS lookup for the host name.
	 *
	 * @param host The host name to resolve.
	 * @return All the ip addresses found for the host name.
	 */
	public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
		Name name = null;

		try {
			name = new Name(host);
		} catch (TextParseException e) {
			throw new UnknownHostException(host);
		}

		Record[] records = null;
		if (preferV6) {
			records = new Lookup(name, Type.AAAA).run();
		}
		if (records == null) {
			records = new Lookup(name, Type.A).run();
		}
		if (records == null && !preferV6) {
			records = new Lookup(name, Type.AAAA).run();
		}
		if (records == null) {
			throw new UnknownHostException(host);
		}

		InetAddress[] array = new InetAddress[records.length];
		for (int i = 0; i < records.length; i++) {
			Record record = records[i];
			if (records[i] instanceof ARecord) {
				ARecord a = (ARecord) records[i];
				array[i] = a.getAddress();
			} else {
				AAAARecord aaaa = (AAAARecord) records[i];
				array[i] = aaaa.getAddress();
			}
		}
		return array;
	}

	/**
	 * Performs a reverse DNS lookup.
	 *
	 * @param addr The ip address to lookup.
	 * @return The host name found for the ip address.
	 */
	public String getHostByAddr(byte[] addr) throws UnknownHostException {
		Name name = ReverseMap.fromAddress(InetAddress.getByAddress(addr));
		Record[] records = new Lookup(name, Type.PTR).run();
		if (records == null) {
			throw new UnknownHostException();
		}
		return ((PTRRecord) records[0]).getTarget().toString();
	}
}
