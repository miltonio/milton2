package io.milton.http.http11.auth;

import io.milton.http.*;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.principal.DiscretePrincipal;
import io.milton.resource.Resource;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This wraps a bunch of other authentication handlers, so if any of those
 * successfully login the user then this will generate a cookie which can be
 * used subsequently.
 *
 * Note that it is usually not correct to wrap a Digest auth handler because
 * that would then defeat the purpose of digest authentication. However, this
 * can and should wrap Basic and Form authentication handlers
 *
 * @author brad
 */
public class CookieAuthenticationHandler implements AuthenticationHandler {

	private static final Logger log = LoggerFactory.getLogger(CookieAuthenticationHandler.class);
	private static final String HANDLER_ATT_NAME = "_delegatedAuthenticationHandler";
	private String requestParamLogout = "miltonLogout";
	private String cookieUserUrlValue = "miltonUserUrl"; // TODO: make this a HTTP Only cookie, to avoid XSS attacks
	private String cookieUserUrlHash = "miltonUserUrlHash";
	private final List<AuthenticationHandler> handlers;
	private final ResourceFactory principalResourceFactory;
	private String userUrlAttName = "userUrl";

	public CookieAuthenticationHandler(List<AuthenticationHandler> handlers, ResourceFactory principalResourceFactory) {
		this.handlers = handlers;
		this.principalResourceFactory = principalResourceFactory;
	}

	@Override
	public boolean supports(Resource r, Request request) {
		// find the authId, if any, from the request
		String userUrl = getUserUrl(request);

		// check for a logout command, if so logout
		if (isLogout(request)) {
			if (userUrl != null && userUrl.length() > 0) {
				clearCookieValue(HttpManager.response());
			}
		}

		for (AuthenticationHandler hnd : handlers) {
			if (hnd.supports(r, request)) {
				request.getAttributes().put(HANDLER_ATT_NAME, hnd);
				return true;
			}
		}

		// We will support it if there is either a auth id request param
		if (userUrl != null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Object authenticate(Resource resource, Request request) {
		log.trace("authenticate");
		// If there is a delegating handler which supports the request then we MUST use it
		// This would have been selected in the supports method
		AuthenticationHandler delegateHandler = (AuthenticationHandler) request.getAttributes().get(HANDLER_ATT_NAME);
		if (delegateHandler != null) {
			// Attempt to authenticate against wrapped handler
			// If successful generate a signed cookie and put into a request attribute
			Object tag = delegateHandler.authenticate(resource, request);
			if (tag != null) {
				if (tag instanceof DiscretePrincipal) {
					DiscretePrincipal p = (DiscretePrincipal) tag;
					setLoginCookies(p, request);
					log.trace("authentication passed by delegated handler, persisted userUrl to cookie");
				} else {
					log.warn("auth.tag is not a " + DiscretePrincipal.class + ", is: " + tag);
				}
				return tag;
			} else {
				log.info("Login failed by delegated handler: " + delegateHandler.getClass());
				return null;
			}
		} else {
			// No delegating handler means that we expect either to get a previous login token
			// via a cookie, or this is an anonymous request
			if (isLogout(request)) {
				return null;
			} else {
				String userUrl = getUserUrl(request);
				if (userUrl == null) {
					// no token in request, so is anonymous
					return null;
				} else {
					// we found a userUrl
					String host = request.getHostHeader();
					Resource r;
					try {
						r = principalResourceFactory.getResource(host, userUrl);
					} catch (NotAuthorizedException ex) {
						log.error("Couldnt check userUrl in cookie", ex);
						r = null;
					} catch (BadRequestException ex) {
						log.error("Couldnt check userUrl in cookie", ex);
						r = null;
					}
					if (r == null) {
						log.warn("User not found host: " + host + " userUrl: " + userUrl + " with resourcefactory: " + principalResourceFactory);
						clearCookieValue(HttpManager.response());
					} else {
						// Logged in ok with details. Check if details came from request parameter, in
						// which case we need to set cookies
						if (r instanceof DiscretePrincipal) {
							DiscretePrincipal dp = (DiscretePrincipal) r;
							setLoginCookies(dp, request);
						} else {
							log.warn("Found user from request, but user object is not expected type. Should be " + DiscretePrincipal.class + " but is " + r.getClass());
						}
					}
					return r;
				}
			}
		}
	}

	/**
	 * Sets cookies to make the given user the currently logged in user for any
	 * subsequent requests.
	 *
	 * And also makes that user the current on-behalf-of user in
	 * CurrentUserService
	 *
	 * @param user
	 * @param request
	 */
	public void setLoginCookies(DiscretePrincipal user, Request request) {
		String userUrl = user.getIdenitifer().getValue();
		setLoginCookies(userUrl, request);
	}

	public void setLoginCookies(String userUrl, Request request) {
		if (request == null) {
			return;
		}

		Response response = HttpManager.response();
		String signing = getUrlSigningHash(userUrl);
		setCookieValues(response, userUrl, signing);
		request.getAttributes().put(userUrlAttName, userUrl);
	}

	public String getUrlSigningHash(String userUrl) {
		String salt = Math.random() + "";
		String signing = salt + ":" + DigestUtils.md5Hex(userUrl + ":" + salt);
		return signing;
	}
	
	@Override
	public String getChallenge(Resource resource, Request request) {
		for (AuthenticationHandler h : handlers) {
			if (h.isCompatible(resource, request)) {
				return h.getChallenge(resource, request);
			}
		}
		throw new UnsupportedOperationException("Not supported because no delegate handler accepted the request");
	}

	@Override
	public boolean isCompatible(Resource resource, Request request) {
		for (AuthenticationHandler h : handlers) {
			if (h.isCompatible(resource, request)) {
				return true;
			}
		}
		return false;
	}

	private boolean isLogout(Request request) {
		if (request.getParams() == null) {
			return false;
		}

		String logoutCommand = request.getParams().get(requestParamLogout);
		return (logoutCommand != null && logoutCommand.length() > 0);
	}

	/**
	 * Find a previous login token in the request, and if present verify its
	 * authenticity via a signing cookie
	 *
	 * @param request
	 * @return
	 */
	private String getUserUrl(Request request) {
		if (request == null) {
			return null;
		}
		String userUrl = getUserUrlFromRequest(request);

		if (userUrl != null) {
			userUrl = userUrl.trim();
			if (userUrl.length() > 0) {
				if (verifyHash(userUrl, request)) {

					return userUrl;
				} else {
					log.error("Invalid userUrl hash, possible attempted hacking attempt");
				}
			}
		}
		return null;
	}

	public String getUserUrlFromRequest(Request request) {
		return getCookieOrParam(request, cookieUserUrlValue);
	}

	public String getHashFromRequest(Request request) {
		String signing = getCookieOrParam(request, cookieUserUrlHash);
		return signing;
	}

	private boolean verifyHash(String userUrl, Request request) {
		String signing = getHashFromRequest(request);
		if (signing == null) {
			return false;
		}
		signing = signing.trim();
		if (signing.length() == 0) {
			return false;
		}
		String[] arr = signing.split(":");
		if (arr.length != 2) {
			return false;
		}
		String salt = arr[0];
		String hash = arr[1];
		String expectedHash = DigestUtils.md5Hex(userUrl + ":" + salt);
		return expectedHash.equals(hash);
	}

	private void setCookieValues(Response response, String userUrl, String hash) {
		response.setCookie(cookieUserUrlValue, userUrl);
		response.setCookie(cookieUserUrlHash, hash);
	}

	private void clearCookieValue(Response response) {
		response.setCookie(cookieUserUrlValue, "");
		response.setCookie(cookieUserUrlHash, "");
	}

	private String getCookieOrParam(Request request, String name) {
		if (request == null) {
			return null;
		}
		if (request.getParams() != null) {
			String v = request.getParams().get(name);
			if (v != null) {
				return v;
			}
		}
		Cookie c = request.getCookie(name);
		if (c != null) {
			return c.getValue();
		}
		return null;
	}

	public String getCookieNameUserUrlHash() {
		return cookieUserUrlHash;
	}

	public String getCookieNameUserUrl() {
		return cookieUserUrlValue;
	}

	public String getUserUrlAttName() {
		return userUrlAttName;
	}

	public void setUserUrlAttName(String userUrlAttName) {
		this.userUrlAttName = userUrlAttName;
	}
	
	
}
