package io.milton.resource;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.principal.PrincipalSearchCriteria;

import java.util.List;

/**
 * Resource implemting this interface will be able to repond to CardDAV addressbook-query
 * @see http://tools.ietf.org/html/rfc6352#section-8.6
 *
 * @author charly-alinto
 * @date 10 sept. 2014
 */
public interface AddressBookQuerySearchableResource
{
	public List<? extends Resource> getChildren( PrincipalSearchCriteria crit ) throws NotAuthorizedException, BadRequestException;
}
