/*
 * Copyright 2012 McEvoy Software Ltd.
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

package io.milton.resource;


/**
 * Represents an entry in an address book.
 *
 * Just a marker interface on top of PropFindableResource, ldap properties are
 * mapped onto dav property names and the normal milton property source processing
 * is applied.
 * 
 * This means that implementations of LdapContact may choose to expose their
 * properties via getters and setters (with BeanPropertyResource annotation)
 * or other property source implementations
 * 
 * @author brad
 */
public interface LdapContact extends PropFindableResource {
	
	
}
