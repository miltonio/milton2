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

package io.milton.ldap;

import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerEncoder;
import io.milton.common.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class LdapResponseHandler {

	private static final Logger log = LoggerFactory.getLogger(LdapResponseHandler.class);
	/**
	 * reusable BER encoder
	 */
	final BerEncoder responseBer = new BerEncoder();
	
	private final Socket client;
	private final OutputStream os;
	/**
	 * Current LDAP version (used for String encoding)
	 */
	int ldapVersion = Ldap.LDAP_VERSION3;
	
	private String currentHostName;

	public LdapResponseHandler(Socket client, OutputStream os) {
		this.client = client;
		this.os = os;
	}
	
	

	public boolean isLdapV3() {
		return ldapVersion == Ldap.LDAP_VERSION3;
	}

	/**
	 * Send Root DSE
	 *
	 * @param currentMessageId current message id
	 * @throws IOException on error
	 */
	public void sendRootDSE(int currentMessageId) throws IOException {
		log.debug("LOG_LDAP_SEND_ROOT_DSE");

		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("objectClass", "top");
		attributes.put("namingContexts", Ldap.NAMING_CONTEXTS);
		//attributes.put("supportedsaslmechanisms", "PLAIN");

		sendEntry(currentMessageId, "Root DSE", attributes);
	}

	public void sendEntry(int currentMessageId, String dn, Map<String, Object> attributes) throws IOException {
		LogUtils.trace(log, "sendEntry", currentMessageId, dn, attributes.size());
		// synchronize on responseBer
		synchronized (responseBer) {
			responseBer.reset();
			responseBer.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
			responseBer.encodeInt(currentMessageId);
			responseBer.beginSeq(Ldap.LDAP_REP_SEARCH);
			responseBer.encodeString(dn, isLdapV3());
			responseBer.beginSeq(Ldap.LBER_SEQUENCE);
			for (Map.Entry<String, Object> entry : attributes.entrySet()) {
				responseBer.beginSeq(Ldap.LBER_SEQUENCE);
				responseBer.encodeString(entry.getKey(), isLdapV3());
				responseBer.beginSeq(Ldap.LBER_SET);
				Object values = entry.getValue();
				if (values instanceof String) {
					responseBer.encodeString((String) values, isLdapV3());
				} else if (values instanceof List) {
					for (Object value : (List) values) {
						responseBer.encodeString((String) value, isLdapV3());
					}
				} else {
					throw new RuntimeException("EXCEPTION_UNSUPPORTED_VALUE: " + values);
				}
				responseBer.endSeq();
				responseBer.endSeq();
			}
			responseBer.endSeq();
			responseBer.endSeq();
			responseBer.endSeq();
			sendResponse();
		}
	}

	public void sendErr(int currentMessageId, int responseOperation, Exception e) throws IOException {
		String message = e.getMessage();
		if (message == null) {
			message = e.toString();
		}
		sendClient(currentMessageId, responseOperation, Ldap.LDAP_OTHER, message);
	}

	public void sendClient(int currentMessageId, int responseOperation, int status, String message) throws IOException {
		responseBer.reset();

		responseBer.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
		responseBer.encodeInt(currentMessageId);
		responseBer.beginSeq(responseOperation);
		responseBer.encodeInt(status, Ldap.LBER_ENUMERATED);
		// dn
		responseBer.encodeString("", isLdapV3());
		// error message
		responseBer.encodeString(message, isLdapV3());
		responseBer.endSeq();
		responseBer.endSeq();
		sendResponse();
	}

	public void sendResponse() throws IOException {
		//Ber.dumpBER(System.out, ">\n", responseBer.getBuf(), 0, responseBer.getDataLen());
		os.write(responseBer.getBuf(), 0, responseBer.getDataLen());
		os.flush();
	}

	void setVersion(int v) {
		ldapVersion = v;
	}

	/**
	 * Send Base Context
	 *
	 * @param currentMessageId current message id
	 * @throws IOException on error
	 */
	public void sendBaseContext(int currentMessageId) throws IOException {
		List<String> objectClasses = new ArrayList<String>();
		objectClasses.add("top");
		objectClasses.add("organizationalUnit");
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("objectClass", objectClasses);
		attributes.put("description", "Milton LDAP Gateway");
		sendEntry(currentMessageId, Ldap.BASE_CONTEXT, attributes);
	}

	/**
	 * Send ComputerContext
	 *
	 * @param currentMessageId current message id
	 * @param returningAttributes attributes to return
	 * @throws IOException on error
	 */
	public void sendComputerContext(int currentMessageId, Set<String> returningAttributes) throws IOException {
		List<String> objectClasses = new ArrayList<String>();
		objectClasses.add("top");
		objectClasses.add("apple-computer");
		Map<String, Object> attributes = new HashMap<String, Object>();
		addIf(attributes, returningAttributes, "objectClass", objectClasses);
		addIf(attributes, returningAttributes, "apple-generateduid", Ldap.COMPUTER_GUID);
		addIf(attributes, returningAttributes, "apple-serviceinfo", getServiceInfo());
		// TODO: remove ?
		addIf(attributes, returningAttributes, "apple-xmlplist", getServiceInfo());
		addIf(attributes, returningAttributes, "apple-serviceslocator", "::anyService");
		addIf(attributes, returningAttributes, "cn", getCurrentHostName());

		String dn = "cn=" + getCurrentHostName() + ", " + Ldap.COMPUTER_CONTEXT;
		log.debug("LOG_LDAP_SEND_COMPUTER_CONTEXT", dn, attributes);

		sendEntry(currentMessageId, dn, attributes);
	}

	protected String getServiceInfo() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<?xml version='1.0' encoding='UTF-8'?>"
				+ "<!DOCTYPE plist PUBLIC '-//Apple//DTD PLIST 1.0//EN' 'http://www.apple.com/DTDs/PropertyList-1.0.dtd'>"
				+ "<plist version='1.0'>"
				+ "<dict>"
				+ "<key>com.apple.macosxserver.host</key>"
				+ "<array>"
				+ "<string>localhost</string>" + // NOTE: Will be replaced by real hostname
				"</array>"
				+ "<key>com.apple.macosxserver.virtualhosts</key>"
				+ "<dict>"
				+ "<key>" + Ldap.VIRTUALHOST_GUID + "</key>"
				+ "<dict>"
				+ "<key>hostDetails</key>"
				+ "<dict>"
				+ "<key>http</key>"
				+ "<dict>"
				+ "<key>enabled</key>"
				+ "<true/>"
				+ "</dict>"
				+ "<key>https</key>"
				+ "<dict>"
				+ "<key>disabled</key>"
				+ "<false/>"
				+ "<key>port</key>"
				+ "<integer>0</integer>"
				+ "</dict>"
				+ "</dict>"
				+ "<key>hostname</key>"
				+ "<string>");
		try {
			buffer.append(getCurrentHostName());
		} catch (UnknownHostException ex) {
			buffer.append("Unknown host");
		}
		buffer.append("</string>"
				+ "<key>serviceInfo</key>"
				+ "<dict>"
				+ "<key>calendar</key>"
				+ "<dict>"
				+ "<key>enabled</key>"
				+ "<true/>"
				+ "<key>templates</key>"
				+ "<dict>"
				+ "<key>calendarUserAddresses</key>"
				+ "<array>"
				+ "<string>%(principaluri)s</string>"
				+ "<string>mailto:%(email)s</string>"
				+ "<string>urn:uuid:%(guid)s</string>"
				+ "</array>"
				+ "<key>principalPath</key>"
				+ "<string>/principals/__uuids__/%(guid)s/</string>"
				+ "</dict>"
				+ "</dict>"
				+ "</dict>"
				+ "<key>serviceType</key>"
				+ "<array>"
				+ "<string>calendar</string>"
				+ "</array>"
				+ "</dict>"
				+ "</dict>"
				+ "</dict>"
				+ "</plist>");

		return buffer.toString();
	}
	

	protected String getCurrentHostName() throws UnknownHostException {
		if (currentHostName == null) {
			if (client.getInetAddress().isLoopbackAddress()) {
				// local address, probably using localhost in iCal URL
				currentHostName = "localhost";
			} else {
				// remote address, send fully qualified domain name
				currentHostName = InetAddress.getLocalHost().getCanonicalHostName();
			}
		}
		return currentHostName;
	}	
	

	public void dumpBer(byte[] inbuf, int offset) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Ber.dumpBER(baos, "LDAP request buffer\n", inbuf, 0, offset);
		try {
			log.debug(new String(baos.toByteArray(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// should not happen
			log.error("", e);
		}
	}
	

	protected void addIf(Map<String, Object> attributes, Set<String> returningAttributes, String name, Object value) {
		if ((returningAttributes.isEmpty()) || returningAttributes.contains(name)) {
			attributes.put(name, value);
		}
	}	

	public void sendBindResponse(int currentMessageId, int status, byte[] serverResponse) throws IOException {
		responseBer.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
		responseBer.encodeInt(currentMessageId);
		responseBer.beginSeq(Ldap.LDAP_REP_BIND);
		responseBer.encodeInt(status, Ldap.LBER_ENUMERATED);
		// server credentials	
		responseBer.encodeString("", isLdapV3());
		responseBer.encodeString("", isLdapV3());
		// challenge or response
		if (serverResponse != null) {
			responseBer.encodeOctetString(serverResponse, 0x87);
		}
		responseBer.endSeq();
		responseBer.endSeq();
		sendResponse();
	}
	
}
