/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.milton.dns.resource;


import java.util.Set;


/**
 * Should return an SOA record and NS records in getRecords()...
 * @author Administrator
 *
 */
public interface ZoneDomainResource extends DomainResource{

	/**
	 * Return a set of all of the domains contained in this zone, which
	 * will be used for transferring out the zone to another nameserver.
	 * Should include a reference to itself in the set. If support for
	 * sending zones isn't required, just return null
	 * 
	 * @return
	 */
	public Set<DomainResource> getDomainResources();

	//public SOARecord getSOARecord();
	//public List<NSRecord< getNSRecords();
}
