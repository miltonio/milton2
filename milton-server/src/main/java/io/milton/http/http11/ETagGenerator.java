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

package io.milton.http.http11;

import io.milton.resource.Resource;

/**
 * Generates ETags, ie entity tags.
 *
 * Custom implementations must be injected into:
 *  - DefaultHttp11ResponseHandler
 *  - DefaultWebDavPropertySource
 *
 * .. or whatever you're using in their place
 *
 * Note that the Http11ResponseHandler interface extends this, since it response
 * handlers logically must know how to generate etags (or that they shouldnt be
 * generated) and it assists with wrapping to expose that functionality, without
 * exposing the dependency directly.
 *
 *
 * HTTP/1.1 origin servers:
      - SHOULD send an entity tag validator unless it is not feasible to
        generate one.

      - MAY send a weak entity tag instead of a strong entity tag, if
        performance considerations support the use of weak entity tags,
        or if it is unfeasible to send a strong entity tag.

      - SHOULD send a Last-Modified value if it is feasible to send one,
        unless the risk of a breakdown in semantic transparency that
        could result from using this date in an If-Modified-Since header
        would lead to serious problems.
 *
 * @author brad
 */
public interface ETagGenerator {
    /**
     * ETag's serve to identify a particular version of a particular resource.
     *
     * If the resource changes, or is replaced, then this value should change
     *
     * @param r - the resource to generate the ETag for
     * @return - an ETag which uniquely identifies this version of this resource
     */
    String generateEtag( Resource r );
}
