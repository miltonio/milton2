package io.milton.principal;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.Resource;
import java.util.List;

/**
 *
 * @author brad
 */
public interface PrincipalSearchService {
    List<DiscretePrincipal> search(PrincipalSearchCriteria criteria, Resource resource) throws NotAuthorizedException, BadRequestException;
}
