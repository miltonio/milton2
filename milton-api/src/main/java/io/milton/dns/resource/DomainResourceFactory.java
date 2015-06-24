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
