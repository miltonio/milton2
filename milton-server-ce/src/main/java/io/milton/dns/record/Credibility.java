/*
 * Copied from the DnsJava project
 *
 * Copyright (c) 1998-2011, Brian Wellington.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package io.milton.dns.record;


/**
 * Constants relating to the credibility of cached data, which is based on
 * the data's source.  The constants NORMAL and ANY should be used by most
 * callers.
 * @see Cache
 * @see Section
 *
 * @author Brian Wellington
 */

public final class Credibility {

private
Credibility() {}

/** A hint or cache file on disk. */
public static final int HINT			= 0;

/** The additional section of a response. */
public static final int ADDITIONAL		= 1;

/** The additional section of a response. */
public static final int GLUE			= 2;

/** The authority section of a nonauthoritative response. */
public static final int NONAUTH_AUTHORITY	= 3;

/** The answer section of a nonauthoritative response. */
public static final int NONAUTH_ANSWER		= 3;

/** The authority section of an authoritative response. */
public static final int AUTH_AUTHORITY		= 4;

/** The answer section of a authoritative response. */
public static final int AUTH_ANSWER		= 4;

/** A zone. */
public static final int ZONE			= 5;

/** Credible data. */
public static final int NORMAL			= 3;

/** Data not required to be credible. */
public static final int ANY			= 1;

}
