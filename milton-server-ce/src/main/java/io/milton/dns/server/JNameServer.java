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
package io.milton.dns.server;
//import jnamed;

import io.milton.common.Service;
import io.milton.dns.resource.ZoneDomainResource;
import io.milton.dns.resource.DomainResourceFactory;
import io.milton.dns.resource.DomainResource;
import io.milton.dns.resource.NonAuthoritativeException;
import io.milton.dns.Name;
import io.milton.dns.TextParseException;
import io.milton.dns.record.CNAMERecord;
import io.milton.dns.record.DClass;
import io.milton.dns.record.DNAMERecord;
import io.milton.dns.record.ExtendedFlags;
import io.milton.dns.record.Flags;
import io.milton.dns.record.Header;
import io.milton.dns.record.Message;
import io.milton.dns.record.NameTooLongException;
import io.milton.dns.record.OPTRecord;
import io.milton.dns.record.Opcode;
import io.milton.dns.record.RRset;
import io.milton.dns.record.Rcode;
import io.milton.dns.record.Record;
import io.milton.dns.record.SOARecord;
import io.milton.dns.record.Section;
import io.milton.dns.record.SetResponse;
import io.milton.dns.record.TSIG;
import io.milton.dns.record.TSIGRecord;
import io.milton.dns.record.Type;
import io.milton.dns.resource.DomainResourceRecord;
import io.milton.dns.utils.RecordTypes;
import io.milton.dns.utils.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JNameServer implements Service {

	private static Logger log = LoggerFactory.getLogger(JNameServer.class.getName());
	static final int FLAG_DNSSECOK = 1;
	static final int FLAG_SIGONLY = 2;
	DomainResourceFactory drf;
	List<InetSocketAddress> sockAddrs = new ArrayList<InetSocketAddress>();
	List<TcpListener> tcpListeners = new ArrayList<TcpListener>();
	List<UdpListener> udpListeners = new ArrayList<UdpListener>();
	private RecordTypes recordTypes = new RecordTypes();
	volatile boolean running;

	public JNameServer(DomainResourceFactory drf, InetSocketAddress... sockAddrs) {
		this.drf = drf;
		if (sockAddrs == null) {
			this.sockAddrs.add(new InetSocketAddress(53));
		} else {
			this.sockAddrs.addAll(Arrays.asList(sockAddrs));
		}
	}

	@Override
	public void start() {
		log.info("Starting DNS server");
		running = true;
		for (InetSocketAddress sockAddr : sockAddrs) {
			log.info("Listening on interface: " + sockAddr);
			TcpListener tl = new TcpListener(sockAddr);
			tcpListeners.add(tl);
			new Thread(tl).start();

			UdpListener ul = new UdpListener(sockAddr);
			udpListeners.add(ul);
			new Thread(ul).start();
		}
		System.out.println("started server");
	}

	@Override
	public void stop() {
		running = false;
		for (TcpListener tl : tcpListeners) {
			tl.close();
		}
		for (UdpListener ul : udpListeners) {
			ul.close();
		}
	}

	private void addRRset(Name name, Message response, RRset rrset, int section, int flags) {
		for (int s = 1; s <= section; s++) {
			if (response.findRRset(name, rrset.getType(), s)) {
				return;
			}
		}
		if ((flags & FLAG_SIGONLY) == 0) {
			Iterator it = rrset.rrs();
			while (it.hasNext()) {
				Record r = (Record) it.next();
				if (r.getName().isWild() && !name.isWild()) {
					r = r.withName(name);
				}
				response.addRecord(r, section);
			}
		}
		if ((flags & (FLAG_SIGONLY | FLAG_DNSSECOK)) != 0) {
			Iterator it = rrset.sigs();
			while (it.hasNext()) {
				Record r = (Record) it.next();
				if (r.getName().isWild() && !name.isWild()) {
					r = r.withName(name);
				}
				response.addRecord(r, section);
			}
		}
	}

	private void addSOA(Message response, SOARecord soaRecord) {
		response.addRecord(soaRecord, Section.AUTHORITY);
	}

	private void addNS(Message response, RRset nsRecords, int flags) {
		// RRset nsRecords = zone.getNS();
		addRRset(nsRecords.getName(), response, nsRecords, Section.AUTHORITY,
				flags);
	}

	private void addGlue(Message response, Name name, int flags) {

		DomainResource dr = getDomainResource(name.toString());
		if( dr == null ) {
			return ;
		}
		Name domainName;
		try {
			domainName = Utils.stringToName(dr.getName());
		} catch (TextParseException ex) {
			throw new RuntimeException(ex);
		}
		RRset a = getRRset(domainName, dr, Type.A, DClass.IN);
		if (a == null) {
			return;
		}
		addRRset(name, response, a, Section.ADDITIONAL, flags);

	}

	private void addAdditional2(Message response, int section, int flags) {
		Record[] records = response.getSectionArray(section);
		for (int i = 0; i < records.length; i++) {
			Record r = records[i];
			Name glueName = r.getAdditionalName();
			if (glueName != null) {
				addGlue(response, glueName, flags);
			}
		}
	}

	private void addAdditional(Message response, int flags) {
		addAdditional2(response, Section.ANSWER, flags);
		addAdditional2(response, Section.AUTHORITY, flags);
	}

	private byte addAnswer(Message response, Name name, int type, int dclass, int iterations, int flags) {
		System.out.println("addAnswer: " + name + " type=" + type + " class=" + dclass);
		SetResponse sr;
		byte rcode = Rcode.NOERROR;

		if (iterations > 6) {
			log.warn("iterations too high");
			return Rcode.NOERROR;
		}

		if (type == Type.SIG || type == Type.RRSIG) {
			type = Type.ANY;
			flags |= FLAG_SIGONLY;
		}


		sr = generateSetResponse(name, type);
		ZoneDomainResource zdr = findBestZone(drf, name);
		if (sr.isUnknown()) {
			//addCacheNS(response, getCache(dclass), name);
		}
		Name domainName;
		try {
			// TODO: got a NPE here
			domainName = Utils.stringToName(zdr.getName());
		} catch (TextParseException ex) {
			System.out.println("parse ex");
			throw new RuntimeException(ex);
		}
		if (sr.isNXDOMAIN()) {
			log.info("is NX domain");
			response.getHeader().setRcode(Rcode.NXDOMAIN);
			if (zdr != null) {
				RRset rrSet = getRRset(domainName, zdr, Type.SOA, DClass.IN);
				if (rrSet != null) {
					addSOA(response, (SOARecord) rrSet.first());
				}
				if (iterations == 0) {
					response.getHeader().setFlag(Flags.AA);
				}
			}
			rcode = Rcode.NXDOMAIN;
		} else if (sr.isNXRRSET()) {
			log.info("isNXRRSET");
			if (zdr != null) {
				RRset rrSet = getRRset(domainName, zdr, Type.SOA, DClass.IN);
				if (rrSet != null) {
					addSOA(response, (SOARecord) rrSet.first());
				}
				if (iterations == 0) {
					response.getHeader().setFlag(Flags.AA);
				}
			}
		} else if (sr.isDelegation()) {
			log.info("delegation");
			RRset nsRecords = sr.getNS();
			addRRset(nsRecords.getName(), response, nsRecords, Section.AUTHORITY, flags);
		} else if (sr.isCNAME()) {
			log.info("isCNAME");
			CNAMERecord cname = sr.getCNAME();
			RRset rrset = new RRset(cname);
			addRRset(name, response, rrset, Section.ANSWER, flags);
			if (zdr != null && iterations == 0) {
				response.getHeader().setFlag(Flags.AA);
			}
			rcode = addAnswer(response, cname.getTarget(), type, dclass,
					iterations + 1, flags);
		} else if (sr.isDNAME()) {
			log.info("isDNAME");
			DNAMERecord dname = sr.getDNAME();
			RRset rrset = new RRset(dname);
			addRRset(name, response, rrset, Section.ANSWER, flags);
			Name newname;
			try {
				newname = name.fromDNAME(dname);
			} catch (NameTooLongException e) {
				return Rcode.YXDOMAIN;
			}
			rrset = new RRset(new CNAMERecord(name, dclass, 0, newname));
			addRRset(name, response, rrset, Section.ANSWER, flags);
			if (zdr != null && iterations == 0) {
				response.getHeader().setFlag(Flags.AA);
			}
			rcode = addAnswer(response, newname, type, dclass, iterations + 1,
					flags);
		} else if (sr.isSuccessful()) {
			RRset[] rrsets = sr.answers();
			for (int i = 0; i < rrsets.length; i++) {
				addRRset(name, response, rrsets[i], Section.ANSWER, flags);
			}
			if (zdr != null) {
				RRset rrSet = getRRset(domainName, zdr, Type.NS, DClass.IN);
				addNS(response, rrSet, flags);
				if (iterations == 0) {
					response.getHeader().setFlag(Flags.AA);
				}
			} else
				;//addCacheNS(response, getCache(dclass), name);
		}
		log.info(" = " + rcode);
		return rcode;
	}

	private SetResponse generateSetResponse(Name name, int type) {

		int labels;
		int olabels;
		int tlabels;
		RRset rrset;
		Name tname;
		Object types;
		SetResponse sr;

		DomainResource dr = null;

		Name origin = new Name(name, name.labels());
		olabels = origin.labels();
		labels = name.labels();

		for (tlabels = olabels; tlabels <= labels; tlabels++) {
			boolean isOrigin = (tlabels == olabels);
			boolean isExact = (tlabels == labels);

			if (isOrigin) {
				tname = origin;
			} else if (isExact) {
				tname = name;
			} else {
				tname = new Name(name, labels - tlabels);
			}


			dr = getDomainResource(tname.toString());

			if (dr == null) {
				continue;
			}

			Name domainName;
			try {
				domainName = Utils.stringToName(dr.getName());
			} catch (TextParseException ex) {
				throw new RuntimeException(ex);
			}

			/* If this is a delegation, return that. */
			if (!(dr instanceof ZoneDomainResource)) {
				RRset ns = getRRset(domainName, dr, Type.NS, DClass.IN);
				if (ns != null) {
					return new SetResponse(SetResponse.DELEGATION, ns);
				}
			}

			/* If this is an ANY lookup, return everything. */
			if (isExact && type == Type.ANY) {
				sr = new SetResponse(SetResponse.SUCCESSFUL);
				RRset[] sets = getAllRRsets(domainName, dr, DClass.IN);
				for (int i = 0; i < sets.length; i++) {
					sr.addRRset(sets[i]);
				}
				return sr;
			}

			/*
			 * If this is the name, look for the actual type or a CNAME.
			 * Otherwise, look for a DNAME.
			 */
			if (isExact) {
				rrset = getRRset(domainName, dr, type, DClass.IN);
				if (rrset != null) {
					sr = new SetResponse(SetResponse.SUCCESSFUL);
					sr.addRRset(rrset);
					return sr;
				}
				rrset = getRRset(domainName, dr, Type.CNAME, DClass.IN);
				if (rrset != null) {
					return new SetResponse(SetResponse.CNAME, rrset);
				}
			} else {
				rrset = getRRset(domainName, dr, Type.DNAME, DClass.IN);
				if (rrset != null) {
					return new SetResponse(SetResponse.DNAME, rrset);
				}
			}

			/* We found the name, but not the type. */
			if (isExact) {
				return SetResponse.ofType(SetResponse.NXRRSET);
			}
		}


		for (int i = 0; i < labels - /*olabels*/ 1; i++) {
			tname = name.wild(i + 1);

			dr = getDomainResource(tname.toString());
			if (dr == null) {
				continue;
			}

			Name domainName;
			try {
				domainName = Utils.stringToName(dr.getName());
			} catch (TextParseException ex) {
				throw new RuntimeException(ex);
			}

			rrset = getRRset(domainName, dr, type, DClass.IN);
			if (rrset != null) {
				sr = new SetResponse(SetResponse.SUCCESSFUL);
				sr.addRRset(rrset);
				return sr;
			}
		}


		return SetResponse.ofType(SetResponse.NXDOMAIN);
	}


	/*
	 * Note: a null return value means that the caller doesn't need to do
	 * anything. Currently this only happens if this is an AXFR request over
	 * TCP.
	 */
	byte[] generateReply(Message query, byte[] in, int length, Socket s)
			throws IOException {
		Header header;
		boolean badversion;
		int maxLength;
		int flags = 0;

		header = query.getHeader();
		if (header.getFlag(Flags.QR)) {
			return null;
		}
		if (header.getRcode() != Rcode.NOERROR) {
			return errorMessage(query, Rcode.FORMERR);
		}
		if (header.getOpcode() != Opcode.QUERY) {
			return errorMessage(query, Rcode.NOTIMP);
		}

		Record queryRecord = query.getQuestion();

		TSIGRecord queryTSIG = query.getTSIG();
		TSIG tsig = null;
		if (queryTSIG != null) {
			/*
			 * tsig = (TSIG) TSIGs.get(queryTSIG.getName()); if (tsig == null ||
			 * tsig.verify(query, in, length, null) != Rcode.NOERROR) return
			 * formerrMessage(in);
			 */
			return formerrMessage(in);
		}

		OPTRecord queryOPT = query.getOPT();
		if (queryOPT != null && queryOPT.getVersion() > 0) {
			badversion = true;
		}

		if (s != null) {
			maxLength = 65535;
		} else if (queryOPT != null) {
			maxLength = Math.max(queryOPT.getPayloadSize(), 512);
		} else {
			maxLength = 512;
		}

		if (queryOPT != null && (queryOPT.getFlags() & ExtendedFlags.DO) != 0) {
			flags = FLAG_DNSSECOK;
		}

		Message response = new Message(query.getHeader().getID());
		response.getHeader().setFlag(Flags.QR);
		if (query.getHeader().getFlag(Flags.RD)) {
			response.getHeader().setFlag(Flags.RD);
		}
		response.addRecord(queryRecord, Section.QUESTION);

		Name name = queryRecord.getName();
		int type = queryRecord.getType();
		int dclass = queryRecord.getDClass();
		if (type == Type.AXFR && s != null) {
			return doAXFR(name, query, tsig, queryTSIG, s);
		}
		if (!Type.isRR(type) && type != Type.ANY) {
			return errorMessage(query, Rcode.NOTIMP);
		}

		byte rcode = addAnswer(response, name, type, dclass, 0, flags);
		if (rcode != Rcode.NOERROR && rcode != Rcode.NXDOMAIN) {
			return errorMessage(query, rcode);
		}

		addAdditional(response, flags);

		if (queryOPT != null) {
			int optflags = (flags == FLAG_DNSSECOK) ? ExtendedFlags.DO : 0;
			OPTRecord opt = new OPTRecord((short) 4096, rcode, (byte) 0,
					optflags);
			response.addRecord(opt, Section.ADDITIONAL);
		}

		response.setTSIG(tsig, Rcode.NOERROR, queryTSIG);
		return response.toWire(maxLength);
	}

	byte[] buildErrorMessage(Header header, int rcode, Record question) {
		Message response = new Message();
		response.setHeader(header);
		for (int i = 0; i < 4; i++) {
			response.removeAllRecords(i);
		}
		if (rcode == Rcode.SERVFAIL) {
			response.addRecord(question, Section.QUESTION);
		}
		header.setRcode(rcode);
		return response.toWire();
	}

	byte[] doAXFR(Name name, Message query, TSIG tsig, TSIGRecord qtsig,
			Socket s) {
		return errorMessage(query, Rcode.REFUSED);
	}

	public byte[] formerrMessage(byte[] in) {
		Header header;
		try {
			header = new Header(in);
		} catch (IOException e) {
			return null;
		}
		return buildErrorMessage(header, Rcode.FORMERR, null);
	}

	public byte[] errorMessage(Message query, int rcode) {
		return buildErrorMessage(query.getHeader(), rcode, query.getQuestion());
	}

	private DomainResource getDomainResource(String domainName) {
		if (domainName.endsWith(".")) {
			domainName = domainName.substring(0, domainName.length() - 1);
		}
		try {
			return drf.getDomainResource(domainName);
		} catch (NonAuthoritativeException e) {
			return null;
		}
	}

	class TcpListener implements Runnable {

		InetAddress addr;
		int port;
		ServerSocket sock;

		TcpListener(InetSocketAddress sa) {
			this.addr = sa.getAddress();
			this.port = sa.getPort();
		}

		@Override
		public void run() {
			serveTCP(addr, port);
		}

		public void close() {
			if (sock != null) {
				try {
					sock.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void TCPclient(Socket s) {
			try {
				int inLength;
				DataInputStream dataIn;
				DataOutputStream dataOut;
				byte[] in;

				InputStream is = s.getInputStream();
				dataIn = new DataInputStream(is);
				inLength = dataIn.readUnsignedShort();
				in = new byte[inLength];
				dataIn.readFully(in);

				Message query = null;
				byte[] response = null;
				try {
					query = new Message(in);
					response = generateReply(query, in, in.length, s);
					if (response == null) {
						return;
					}
				} catch (IOException e) {
					log.error("exception", e);
					response = formerrMessage(in);
				} catch (RuntimeException e) {
					log.error("exception", e);
					if (query != null) {
						response = errorMessage(query, Rcode.SERVFAIL);
					} else {
						response = formerrMessage(in);
					}

				}
				dataOut = new DataOutputStream(s.getOutputStream());
				dataOut.writeShort(response.length);
				dataOut.write(response);
			} catch (IOException e) {
				System.out.println("TCPclient("
						+ addrport(s.getLocalAddress(), s.getLocalPort()) + "): "
						+ e);
			} finally {
				try {
					s.close();
				} catch (IOException e) {
				}
			}
		}

		public void serveTCP(InetAddress addr, int port) {
			try {
				sock = new ServerSocket(port, 128, addr);
				while (running) {
					final Socket s = sock.accept();
					Thread t;
					t = new Thread(new Runnable() {
						public void run() {
							TCPclient(s);
						}
					});
					t.start();
				}
			} catch (IOException e) {
				System.out.println("serveTCP(" + addrport(addr, port) + "): " + e);
			}
		}
	}

	class UdpListener implements Runnable {

		DatagramSocket sock;
		InetAddress addr;
		int port;

		UdpListener(InetSocketAddress sa) {
			this.addr = sa.getAddress();
			this.port = sa.getPort();
		}

		@Override
		public void run() {
			serveUDP(addr, port);
		}

		public void close() {
			if (sock != null) {
				try {
					sock.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void serveUDP(InetAddress addr, int port) {
			try {
				DatagramSocket sock = new DatagramSocket(port, addr);

				final short udpLength = 512;
				byte[] in = new byte[udpLength];
				DatagramPacket indp = new DatagramPacket(in, in.length);
				DatagramPacket outdp = null;
				while (running) {
					indp.setLength(in.length);
					try {
						sock.receive(indp);
					} catch (InterruptedIOException e) {
						continue;
					}
					Message query = null;
					byte[] response = null;
					try {
						query = new Message(in);
						response = generateReply(query, in, indp.getLength(), null);
						if (response == null) {
							continue;
						}
					} catch (IOException e) {
						log.error("Exeption generating DNS response", e);
						response = formerrMessage(in);
					} catch (RuntimeException e) {
						log.error("Exeption generating DNS response", e);
						if (query != null) {
							response = errorMessage(query, Rcode.SERVFAIL);
						} else {
							response = formerrMessage(in);
						}
					}
					if (outdp == null) {
						outdp = new DatagramPacket(response, response.length,
								indp.getAddress(), indp.getPort());
					} else {
						outdp.setData(response);
						outdp.setLength(response.length);
						outdp.setAddress(indp.getAddress());
						outdp.setPort(indp.getPort());
					}
					sock.send(outdp);
				}
			} catch (IOException e) {
				System.out.println("serveUDP(" + addrport(addr, port) + "): " + e);
			}
		}
	}

	private RRset getRRset(Name domainName, DomainResource dr, int type, int dclass) {

		if (dr == null) {
			return null;
		}
		List<DomainResourceRecord> allRecords = dr.getRecords();
		if (allRecords == null || allRecords.isEmpty()) {
			return null;
		}

		try {
			RRset rrset = new RRset();
			boolean empty = true;
			for (DomainResourceRecord dnsRec : allRecords) {
				System.out.println("check: " + dnsRec.getClass());
				Record rr;
				try {
					rr = recordTypes.map(domainName, dnsRec);
				} catch (TextParseException ex) {
					throw new RuntimeException(ex);
				}
				if (rr.getType() == type && rr.getDClass() == dclass) {
					rrset.addRR(rr);
					empty = false;
				}
			}
			if (empty) {
				return null;
			}
			return rrset;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private RRset[] getAllRRsets(Name domainName, DomainResource dr, int dclass) {

		if (dr == null) {
			return null;
		}
		List<RRset> rrSets = new ArrayList<RRset>();
		List<DomainResourceRecord> allRecords = dr.getRecords();

		for (DomainResourceRecord dnsRec : allRecords) {
			Record rr;
			try {
				rr = recordTypes.map(domainName, dnsRec);
			} catch (TextParseException ex) {
				throw new RuntimeException(ex);
			}
			boolean added = false;
			for (RRset rrSet : rrSets) {
				if (rrSet.getType() == rr.getType()
						&& rrSet.getDClass() == rr.getDClass()) {
					rrSet.addRR(rr);
					added = true;
					break;
				}
			}
			if (!added) {
				RRset rrSet = new RRset();
				rrSet.addRR(rr);
				rrSets.add(rrSet);
			}
		}
		RRset[] rrSetArray = new RRset[rrSets.size()];
		return rrSets.toArray(rrSetArray);
	}

	public ZoneDomainResource findBestZone(DomainResourceFactory drf, Name name) {
		ZoneDomainResource zdr = null;
		for (int tlabels = name.labels(); tlabels > 0; tlabels--) {

			Name tname = new Name(name, name.labels() - tlabels);
			DomainResource dr = getDomainResource(Utils.nameToString(tname));
			if (dr != null && dr instanceof ZoneDomainResource) {
				zdr = (ZoneDomainResource) dr;
				break;
			}
		}
		return zdr;
	}

	private String addrport(InetAddress addr, int port) {
		return addr.getHostAddress() + "#" + port;
	}
}
