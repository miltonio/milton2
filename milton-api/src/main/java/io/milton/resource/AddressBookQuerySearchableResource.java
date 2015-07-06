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
