// Copyright (c) 2003-2004 Brian Wellington (bwelling@xbill.org)

package io.milton.dns.record;

/**
 * An exception thrown when an invalid dclass code is specified.
 *
 * @author Brian Wellington
 */

public class InvalidDClassException extends IllegalArgumentException {

public
InvalidDClassException(int dclass) {
	super("Invalid DNS class: " + dclass);
}

}
