package io.milton.resource;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;

import java.net.URI;
import java.util.Map;

/**
 * CollectionResource which supports sync-token.
 * @author pomu0325
 *
 */
public interface SyncCollectionResource extends CollectionResource {
  /**
   * Current sync-token.
   * @return
   */
  URI getSyncToken();
  
  /**
   * @return Map contains href -> Resource. Removed resource should be included as {@link io.milton.resource.RemovedResource}.
   */
  Map<String, Resource> findResourcesBySyncToken(URI syncToken) throws NotAuthorizedException, BadRequestException;

  /* 3.4.  Types of Changes Reported on Initial Synchronization

   When the DAV:sync-collection request contains an empty DAV:sync-token
   element, the server MUST return all member URLs of the collection
   (taking account of the DAV:sync-level XML element value as per
   Section 3.3, and optional truncation of the result set as per
   Section 3.6) and it MUST NOT return any removed member URLs.  All
   types of member (collection or non-collection) MUST be reported.
   */
}
