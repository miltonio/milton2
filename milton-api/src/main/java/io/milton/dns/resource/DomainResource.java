package io.milton.dns.resource;

import java.util.List;



public interface DomainResource {

	/**
	 * Return the name of this domain. Names with and without the terminating
	 * "." are permitted
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * Return all the resource records associated with this domain
	 * @return
	 */
	public List<DomainResourceRecord> getRecords();
	
	//public boolean isZone();
	//public List<RRset> getAllRecords();
	//public List<RRset> getRecords(int type);
}
