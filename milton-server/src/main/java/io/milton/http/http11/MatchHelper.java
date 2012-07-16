/*
 * Copyright (C) 2012 McEvoy Software Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.milton.http.http11;

import io.milton.http.Request;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class MatchHelper {

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
	 * Ie, returning "true" means to continue with PUT processing. Returning "false"
	 * means that the comparison indicates that processing should not continue
	 *
	 * @param r
	 * @param req
	 * @return
	 */
	public boolean checkIfMatch(Resource r, Request req) {
		String h = req.getIfMatchHeader();
		if (h == null || h.length()==0) {
			return true; // no if-match header, return true so processing continues
		}
		String currentEtag = eTagGenerator.generateEtag(r);
		if (currentEtag == null || currentEtag.length()==0) {
			return false; // no etag on the resource, but an etag was given in header, so fail
		}
		List<String> etags = splitToList(h);
		for (String requestedEtag : etags) {
			if (requestedEtag.equals(currentEtag)) {
				return true; // found a matching tag, return true to continue
			}
		}
		return false; // a if-match header was sent, but a matching tag is not present, so return false
	}

	/**
	 * Returns true if none of the given etags match those given in the if-none-match header
	 * 
	 * This is a fail-safe method. Returning false means "do nothing different", ie continue processing.
	 *
	 * @param handler
	 * @param req
	 * @return
	 */
	public boolean checkIfNoneMatch(Resource r, Request req) {
		String h = req.getIfNoneMatchHeader();
		if (h == null) {
			return false;
		}
		String currentEtag = eTagGenerator.generateEtag(r);
		if (currentEtag == null) {
			return false;
		}
		List<String> etags = splitToList(h);
		for (String requestedEtag : etags) {
			if (requestedEtag.equals(currentEtag)) {
				return true;
			}
		}
		return false;
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
}
