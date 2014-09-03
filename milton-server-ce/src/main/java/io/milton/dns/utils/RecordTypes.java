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
package io.milton.dns.utils;

import io.milton.dns.Address;
import io.milton.dns.Name;
import io.milton.dns.TextParseException;
import io.milton.dns.record.AAAARecord;
import io.milton.dns.record.ARecord;
import io.milton.dns.record.DClass;
import io.milton.dns.record.MXRecord;
import io.milton.dns.record.NSRecord;
import io.milton.dns.record.Record;
import io.milton.dns.record.SOARecord;
import io.milton.dns.resource.ADomainResourceRecord;
import io.milton.dns.resource.DomainResourceRecord;
import io.milton.dns.resource.MXDomainResourceRecord;
import io.milton.dns.resource.NSDomainResourceRecord;
import io.milton.dns.resource.SOADomainResourceRecord;
import java.net.InetAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author brad
 */
public class RecordTypes {

	private final Map<Class<? extends DomainResourceRecord>, RecordMapper> mappers;

	public RecordTypes() {
		this.mappers = new ConcurrentHashMap<Class<? extends DomainResourceRecord>, RecordTypes.RecordMapper>();
		mappers.put(ADomainResourceRecord.class, new ARecordMapper());
		mappers.put(MXDomainResourceRecord.class, new MXRecordMapper());
		mappers.put(NSDomainResourceRecord.class, new NSRecordMapper());
		mappers.put(SOADomainResourceRecord.class, new SOARecordMapper());
	}

	public Record map(Name domainName, DomainResourceRecord r) throws TextParseException {
		RecordMapper mapper = get(r);
		if (mapper != null) {
			return mapper.map(domainName, r);
		} else {
			return null;
		}
	}

	private RecordMapper get(DomainResourceRecord r) {
		Class c = r.getClass();
		for( Entry<Class<? extends DomainResourceRecord>, RecordMapper> entry : mappers.entrySet()) {
			if( entry.getKey().isAssignableFrom(c)) {
				return entry.getValue();
			}
		}
		return null;
	}

	public interface RecordMapper<T extends DomainResourceRecord> {

		Record map(Name domainName, T r) throws TextParseException;
	}

	public class ARecordMapper implements RecordMapper<ADomainResourceRecord> {

		@Override
		public Record map(Name domainName, ADomainResourceRecord r) throws TextParseException {
			// Not sure, are we supposed to build the domain name into the record name???
			//Name name = Name.fromString(r.getName(), domainName);
			Name name = Utils.stringToName(r.getName());
			Record arr;
			InetAddress add = r.getAddress();
			if (Address.familyOf(add) == Address.IPv4) {
				arr = new ARecord(name, DClass.IN, r.getTtl(), add);
			} else if (Address.familyOf(add) == Address.IPv4) {
				arr = new AAAARecord(name, DClass.IN, r.getTtl(), add);
			} else {
				throw new RuntimeException("Unknown address type: " + add.getCanonicalHostName());
			}
			return arr;
		}
	}

	public class MXRecordMapper implements RecordMapper<MXDomainResourceRecord> {

		@Override
		public Record map(Name domainName, MXDomainResourceRecord r) throws TextParseException {
			Name thisName = Utils.stringToName(r.getName());
			if( thisName == null ) {
				throw new RuntimeException("resource name is null: " + r.getClass() + " - " + r.getName());				 
			}
			Name targetName = Utils.stringToName(r.getTarget());
			if( targetName == null ) {
				throw new RuntimeException("targetName name is null: " + r.getClass() + " - " + r.getTarget());
			}			
			MXRecord mxrr = new MXRecord(thisName, DClass.IN, r.getTtl(), r.getPriority(), targetName);
			return mxrr;
		}
	}

	public class NSRecordMapper implements RecordMapper<NSDomainResourceRecord> {

		@Override
		public Record map(Name domainName, NSDomainResourceRecord r) throws TextParseException {
			Name thisName = Utils.stringToName(r.getName());
			Name targetName = Utils.stringToName(r.getTarget());
			NSRecord mxrr = new NSRecord(thisName, DClass.IN, r.getTtl(), targetName);
			return mxrr;
		}
	}

	public class SOARecordMapper implements RecordMapper<SOADomainResourceRecord> {

		@Override
		public Record map(Name domainName, SOADomainResourceRecord r) throws TextParseException {
			Name thisName = Utils.stringToName(r.getName());
			Name hostName = Utils.stringToName(r.getHost());
			String adminEmail = r.getAdminEmail();
			if( adminEmail != null ) {
				if( adminEmail.contains("@")) {
					adminEmail = adminEmail.replace("@", ".");
				}
			}
			Name adminEmailName = Utils.stringToName(adminEmail);
			SOARecord soarr = new SOARecord(thisName, DClass.IN, r.getTtl(),
					hostName, adminEmailName, r.getZoneSerialNumber(),
					r.getRefresh(), r.getRetry(), r.getExpire(),
					r.getMinimum());
			return soarr;
		}
	}
}
