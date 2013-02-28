package io.milton.dns.resource;

import java.util.Set;




public interface DomainResourceFactory {

	/**
	 * Return a DomainResource for the given domain name, or null if the name
	 * does not exist. Throwing a NonAuthoritativeException signifies that the
	 * nameserver does not have authoritative data for the requested name, while
	 * a null response states definitively that there is no such domain.
	 * 
	 * The method must return a ZoneDomainResource for any domain that marks the
	 * start of a zone of authority.
	 * 
	 * Implementations should be able to handle domain name Strings that end in a "."
	 * as well as those that don't.
	 * 
	 * @param domainName
	 * @return
	 * @throws NonAuthoritativeException
	 */
	public DomainResource getDomainResource(String domainName) throws NonAuthoritativeException;
	/*
	 * Takes a String rather than a Name, because implementations may be part of
	 * a project that uses its own name class
	 */
}
