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
package io.milton.http.http11;

import io.milton.http.Request;
import io.milton.resource.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class MatchHelper {

	private static final Logger log = LoggerFactory.getLogger(MatchHelper.class);

	private final ETagGenerator eTagGenerator;

	public MatchHelper(ETagGenerator eTagGenerator) {
		this.eTagGenerator = eTagGenerator;
	}

	/**
	 * Check if the resource has been modified based on etags
	 *
	 * Returns true if the match comparison indicates that the resource has NOT
	 * been modified
	 *
	 * Ie, returning "true" means to continue with PUT processing. Returning
	 * "false" means that the comparison indicates that processing should not
	 * continue
	 *
	 * @param r
	 * @param req
	 * @return - true means resource is not modified
	 */
	public boolean checkIfMatch(Resource r, Request req) {
		if( r == null ) {
			String h = req.getIfMatchHeader();
			if( h == null ) {
				return true;
			}
			return false;
		}
		Boolean result = _checkIfMatch(r, req);
		if (result != null) {
			// got a result, so use it
			return result;
		}
		// No opinion from if-match header, so also check If header
		String value = req.getIfHeader();
		if (value == null) {
			// no if header, return true so processing continues
			return true;
		}
		Pattern pattern = Pattern.compile(".*\\[\"(.*)\"\\]\\)$");
		Matcher m = pattern.matcher(value);
		if (!m.matches()) {
			// If header doesn't contain an etag, so nothing to check, all good..
			return true;
		}
		String etag = m.group(1);
		return checkIfMatch(r, etag);
	}

	/**
	 * The original checkIfMatch method ..
	 *
	 * @param r
	 * @param req
	 * @return
	 */
	private Boolean _checkIfMatch(Resource r, Request req) {
		String h = req.getIfMatchHeader();
		if (h == null || h.length() == 0) {
			return null; // no if-match header, return true so processing continues
		}
		if (r == null) {
			return false; // etag given, but no resource. Definitely not a match
		}
		String currentEtag = eTagGenerator.generateEtag(r);
		if (currentEtag == null || currentEtag.length() == 0) {
			return false; // no etag on the resource, but an etag was given in header, so fail
		}
		List<String> etags = splitToList(h);
		for (String requestedEtag : etags) {
			requestedEtag = cleanUp(requestedEtag);
			if (requestedEtag.equals(currentEtag) || requestedEtag.equals("*")) {
				return true; // found a matching tag, return true to continue
			}
		}
		log.debug("Did not find matching etag");
		return false; // a if-match header was sent, but a matching tag is not present, so return false
	}



	private boolean checkIfMatch(Resource r, String requestedEtag) {
		if (r == null) {
			return false; // etag given, but no resource. Definitely not a match
		}
		String currentEtag = eTagGenerator.generateEtag(r);
		if (currentEtag == null || currentEtag.length() == 0) {
			return false; // no etag on the resource, but an etag was given in header, so fail
		}
		requestedEtag = cleanUp(requestedEtag);
		//System.out.println("checkIfMatch: compare: " + requestedEtag + " = " + currentEtag);
		if (requestedEtag.equals(currentEtag) || requestedEtag.equals("*")) {
			return true; // found a matching tag, return true to continue
		}
		return false; // a if-match header was sent, but a matching tag is not present, so return false
	}

	/**
	 * Returns true if none of the given etags match those given in the
	 * if-none-match header
	 *
	 * In the usual use case of GET returning false means "do nothing
	 * different", ie continue processing.
	 *
	 * @param r
	 * @param req
	 * @return
	 */
	public boolean checkIfNoneMatch(Resource r, Request req) {
		String h = req.getIfNoneMatchHeader();
		if (h == null) {
			return false;
		}
		if (h.equals("*")) {
			boolean b = (r != null);
			if (r != null) {
				log.warn("if-none-match header is star, and a resource exists, so check has failed: resource name={}", r.getName());
				return true;
			}
			return b;
		}
		String currentEtag = eTagGenerator.generateEtag(r);
		if (currentEtag == null) {
			log.warn("Null etag for resource, so pass if-none-match test");
			return false;
		}
		List<String> etags = splitToList(h);
		for (String requestedEtag : etags) {
			if (requestedEtag.equals(currentEtag)) {
				return true;
			}
		}
		log.warn("None of the provided etags match, so if-none-match test passes");
		return false;
	}

	/**
	 * Only used if a Range header is in the request. Check for the presence of
	 * a If-Range header , if present check if it contains the current etag for
	 * the resource, and if so continue with the partial GET.
	 *
	 * If the given etag is not valid return false, which indicates that a
	 * normal GET with full content should be performed
	 *
	 * If there is no If-Range header returns true to indicate the partial GET
	 * should proceed normally
	 */
	public boolean checkIfRange(Resource r, Request request) {
		String requestedEtag = request.getIfRangeHeader();
		if (requestedEtag == null || requestedEtag.trim().length() == 0) {
			return true; // continue normally
		}
		String currentEtag = eTagGenerator.generateEtag(r);
		if (currentEtag == null || currentEtag.length() == 0) {
			return false; // no etag on the resource, but an etag was given in header, so fail
		}
		if (requestedEtag.equals(currentEtag) || requestedEtag.equals("*")) {
			return true; // found a matching tag, return true to continue
		}

		return false; // a if-match header was sent, but a matching tag is not present, so return false

	}

	private List<String> splitToList(String s) {
		String[] arr = s.split(",");
		List<String> list = new ArrayList<String>();
		for (String part : arr) {
			part = part.trim();
			if (part.length() > 0) {
				list.add(part.trim());
			}
		}
		return list;
	}

	/**
	 * Some user agents encode the quotes we send them in the etag
	 *
	 * @param s
	 * @return
	 */
	private String cleanUp(String s) {
		s = s.replace("&quot;", "");
		s = s.replace("\"\"", "\"");
		return s;
	}
}
