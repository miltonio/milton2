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
	public Set<DomainResource> getDomainResoures();
	
	//public SOARecord getSOARecord();
	//public List<NSRecord< getNSRecords();
}
