package io.milton.examples.security.advanced;

import io.milton.http.AuthenticationHandler;
import io.milton.http.Request;
import io.milton.resource.Resource;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * This is an example of a non-standard authentication handler.
 * 
 * Normally authentication is done using HTTP Basic or Digest mechanisms. Often, 
 * however, we want authentication to be done using some other mechanism, such
 * as reading a token in the URL or using some federated framework like SAML
 * 
 * This is a deliberately facetious example to show authentication being 
 * performed with a completely non-standard mechanism, where the principal is 
 * simply the day of the week. ie on Tuesday all requests will be authenticated as
 * the user 'Tuesday'
 *
 * @author brad
 */
public class DayOfWeekAuthenticationHandler implements AuthenticationHandler {

    public boolean supports(Resource r, Request request) {
        return true;
    }

    public Object authenticate(Resource resource, Request request) {
        Calendar cal = Calendar.getInstance();
        String s = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);
        System.out.println("Authenticated as: " + s);
        return s;
    }

    public void appendChallenges(Resource resource, Request request, List<String> challenges) {
        // we will never append HTTP challenges
    }

    public boolean isCompatible(Resource resource, Request request) {
        return true;
    }

    public boolean credentialsPresent(Request request) {
        return true;
    }

}
