package io.milton.http.acl;

import io.milton.http.annotated.AnnoCollectionResource;
import io.milton.http.annotated.AnnoPrincipalResource;
import io.milton.http.annotated.AnnoResource;
import io.milton.http.annotated.AnnotationResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.principal.DiscretePrincipal;
import io.milton.principal.PrincipalSearchCriteria;
import io.milton.principal.PrincipalSearchService;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a default implementation of principal search which iterates over the
 * principals in the users home and applies critieria.
 *
 * It should generally not be used in production. Instead you should replace it
 * with something that integrates into the query facilities of your persistence
 * tier to search efficiently.
 *
 * @author brad
 */
public class AnnotationsPrincipalSearchService implements PrincipalSearchService {

    private static final Logger log = LoggerFactory.getLogger(AnnotationsPrincipalSearchService.class);

    private AnnotationResourceFactory arf;

    public AnnotationsPrincipalSearchService() {
    }

    @Override
    public List<DiscretePrincipal> search(PrincipalSearchCriteria criteria, Resource resource) throws BadRequestException, NotAuthorizedException {
        log.info("search. Resource=" + resource + "Criteria:" + criteria);
        if (resource instanceof AnnoResource) {
            AnnoResource cr = (AnnoResource) resource;
            List<AnnoCollectionResource> usersCol = arf.getUsersAnnotationHandler().findUsersCollections(cr.getRoot());
            if (usersCol == null) {
                log.warn("No users collection found");
                return null;
            }
            List<DiscretePrincipal> results = new ArrayList<>();
            for (AnnoCollectionResource r : usersCol) {
                for (Resource u : Optional.ofNullable(r.getChildren()).orElse(List.of())) {
                    if (u instanceof AnnoPrincipalResource) {
                        AnnoPrincipalResource dp = (AnnoPrincipalResource) u;
                        for (PrincipalSearchCriteria.SearchItem item  : criteria.getSearchItems()) {
                            String searchVal = item.getValue();
                            log.info("Search val=" + searchVal);
                            if (isMatch(dp, searchVal)) {
                                results.add(dp);
                                break;
                            }
                        }
                    }
                }
            }
            return results;
        } else {
            log.warn("resource is not a AnnoResource, so can search");
            return null;
        }
    }

    private boolean isMatch(AnnoPrincipalResource dp, String searchVal) {
        String dname = dp.getDisplayName();
        String email = dp.getEmail();
        StringBuilder sb = new StringBuilder();
        if (dname != null) {
            sb.append(dname.toLowerCase());
        }
        if (email != null) {
            sb.append(" ");
            sb.append(email.toLowerCase());
        }
        return sb.toString().contains(searchVal.toLowerCase());
    }

    public AnnotationResourceFactory getAnnotationResourceFactory() {
        return arf;
    }

    public void setAnnotationResourceFactory(AnnotationResourceFactory arf) {
        this.arf = arf;
    }
}
