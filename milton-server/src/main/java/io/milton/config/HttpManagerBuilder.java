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
package io.milton.config;

import io.milton.common.Stoppable;
import io.milton.event.EventManager;
import io.milton.event.EventManagerImpl;
import io.milton.http.*;
import io.milton.http.acl.ACLProtocol;
import io.milton.http.acl.AccessControlledResourceTypeHelper;
import io.milton.http.caldav.CalDavProtocol;
import io.milton.http.caldav.CalendarResourceTypeHelper;
import io.milton.http.carddav.AddressBookResourceTypeHelper;
import io.milton.http.carddav.CardDavProtocol;
import io.milton.http.entity.DefaultEntityTransport;
import io.milton.http.entity.EntityTransport;
import io.milton.http.http11.*;
import io.milton.http.http11.DefaultHttp11ResponseHandler.BUFFERING;
import io.milton.http.http11.auth.*;
import io.milton.http.json.JsonResourceFactory;
import io.milton.http.quota.QuotaDataAccessor;
import io.milton.http.values.ValueWriters;
import io.milton.http.webdav.*;
import io.milton.property.DefaultPropertyAuthoriser;
import io.milton.property.PropertyAuthoriser;
import io.milton.property.PropertySource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the options for configuring a HttpManager. To use it just set
 * properties on this class, then call init, then call buildHttpManager to get a
 * reference to the HttpManager.
 *
 * Note that this uses a two-step construction process: init()
 * buildHttpManager()
 *
 * The first step creates instances of any objects which have not been set and
 * the second binds them onto the HttpManager. You might want to modify the
 * objects created in the first step, eg setting properties on default
 * implementations. Note that you should not modify the structure of the
 * resultant object graph, because you could then end up with an inconsistent
 * configuration
 *
 * Where possible, default implementations are created when this class is
 * constructed allowing them to be overwritten where needed. However this is
 * only done for objects and values which are "leaf" nodes in the config object
 * graph. This is to avoid inconsistent configuration where different parts of
 * milton end up with different implementations of the same concern. For
 * example, PropFind and PropPatch could end up using different property sources
 *
 * @author brad
 */
public class HttpManagerBuilder {

	private static final Logger log = LoggerFactory.getLogger(HttpManager.class);
	private ResourceFactory mainResourceFactory;
	private ResourceFactory outerResourceFactory;
	private DefaultHttp11ResponseHandler.BUFFERING buffering;
	private List<AuthenticationHandler> authenticationHandlers;
	private List<AuthenticationHandler> cookieDelegateHandlers;
	private DigestAuthenticationHandler digestHandler;
	private BasicAuthHandler basicHandler;
	private CookieAuthenticationHandler cookieAuthenticationHandler;
	private FormAuthenticationHandler formAuthenticationHandler;
	private Map<UUID, Nonce> nonces = new ConcurrentHashMap<UUID, Nonce>();
	private int nonceValiditySeconds = 60 * 60 * 24;
	private NonceProvider nonceProvider;
	private AuthenticationService authenticationService;
	private ExpiredNonceRemover expiredNonceRemover;
	private List<Stoppable> shutdownHandlers = new CopyOnWriteArrayList<Stoppable>();
	private ResourceTypeHelper resourceTypeHelper;
	private WebDavResponseHandler webdavResponseHandler;
	private HandlerHelper handlerHelper;
	private ArrayList<HttpExtension> protocols;
	private ProtocolHandlers protocolHandlers;
	private EntityTransport entityTransport = new DefaultEntityTransport();
	private List<WellKnownResourceFactory.WellKnownHandler> wellKnownHandlers;
	private EventManager eventManager = new EventManagerImpl();
	private PropertyAuthoriser propertyAuthoriser = new DefaultPropertyAuthoriser();
	private List<PropertySource> propertySources;
	private List<PropertySource> extraPropertySources;
	private ETagGenerator eTagGenerator = new DefaultETagGenerator();
	private Http11ResponseHandler http11ResponseHandler;
	private ValueWriters valueWriters = new ValueWriters();
	private PropFindXmlGenerator propFindXmlGenerator;
	private List<Filter> filters;
	private Filter defaultStandardFilter = new StandardFilter();
	private UrlAdapter urlAdapter = new UrlAdapterImpl();
	private QuotaDataAccessor quotaDataAccessor;
	private PropPatchSetter propPatchSetter;
	private boolean enableOptionsAuth = false;
	private ResourceHandlerHelper resourceHandlerHelper;
	private boolean initDone;
	private boolean enableCompression = true;
	private boolean enableWellKnown = true;
	private boolean enabledJson = true;
	private boolean enableBasicAuth = true;
	private boolean enableDigestAuth = true;
	private boolean enableFormAuth = true;
	private boolean enableCookieAuth = true;
	private String loginPage = "/login.html";
	private List<String> loginPageExcludePaths;

	/**
	 * This method creates instances of required objects which have not been set
	 * on the builder.
	 *
	 * These are subsequently wired together immutably in HttpManager when
	 * buildHttpManager is called.
	 *
	 * You can call this before calling buildHttpManager if you would like to
	 * modify property values on the created objects before HttpManager is
	 * instantiated. Otherwise, you can call buildHttpManager directly and it
	 * will call init if it has not been called
	 *
	 */
	public final void init() {
		if (mainResourceFactory == null) {
			throw new IllegalStateException("Please provide a resource factory"); // todo: defaul to FS?
		}
		if (authenticationService == null) {
			if (authenticationHandlers == null) {
				authenticationHandlers = new ArrayList<AuthenticationHandler>();
				if (basicHandler == null) {
					if (enableBasicAuth) {
						basicHandler = new BasicAuthHandler();
					}
				}
				if (basicHandler != null) {
					authenticationHandlers.add(basicHandler);
				}
				if (digestHandler == null) {
					if (enableDigestAuth) {
						if (nonceProvider == null) {
							if (expiredNonceRemover == null) {
								expiredNonceRemover = new ExpiredNonceRemover(nonces, nonceValiditySeconds);
								showLog("expiredNonceRemover", expiredNonceRemover);
							}
							nonceProvider = new SimpleMemoryNonceProvider(nonceValiditySeconds, expiredNonceRemover, nonces);
							showLog("nonceProvider", nonceProvider);
						}
						digestHandler = new DigestAuthenticationHandler(nonceProvider);
					}
				}
				if (digestHandler != null) {
					authenticationHandlers.add(digestHandler);
				}
				if (formAuthenticationHandler == null) {
					if (enableFormAuth) {
						formAuthenticationHandler = new FormAuthenticationHandler();
					}
				}
				if (formAuthenticationHandler != null) {
					authenticationHandlers.add(formAuthenticationHandler);
				}
				if (cookieAuthenticationHandler == null) {
					if (enableCookieAuth) {
						if (cookieDelegateHandlers == null) {
							// Don't add digest!
							cookieDelegateHandlers = new ArrayList<AuthenticationHandler>();
							if (basicHandler != null) {
								cookieDelegateHandlers.add(basicHandler);
								authenticationHandlers.remove(basicHandler);
							}
							if (formAuthenticationHandler != null) {
								cookieDelegateHandlers.add(formAuthenticationHandler);
								authenticationHandlers.remove(formAuthenticationHandler);
							}
						}
						cookieAuthenticationHandler = new CookieAuthenticationHandler(cookieDelegateHandlers, mainResourceFactory);
						authenticationHandlers.add(cookieAuthenticationHandler);
					}
				}
			}
			authenticationService = new AuthenticationService(authenticationHandlers);
			showLog("authenticationService", authenticationService);
		}

		init(authenticationService);
		shutdownHandlers.add(expiredNonceRemover);
		expiredNonceRemover.start();
	}

	private void init(AuthenticationService authenticationService) {
		// build a stack of resource type helpers
		if (resourceTypeHelper == null) {
			WebDavResourceTypeHelper webDavResourceTypeHelper = new WebDavResourceTypeHelper();
			AccessControlledResourceTypeHelper accessControlledResourceTypeHelper = new AccessControlledResourceTypeHelper(webDavResourceTypeHelper);
			CalendarResourceTypeHelper calendarResourceTypeHelper = new CalendarResourceTypeHelper(accessControlledResourceTypeHelper);
			resourceTypeHelper = new AddressBookResourceTypeHelper(calendarResourceTypeHelper);
			showLog("resourceTypeHelper", resourceTypeHelper);
		}

		if (webdavResponseHandler == null) {
			if (propFindXmlGenerator == null) {
				propFindXmlGenerator = new PropFindXmlGenerator(valueWriters);
				showLog("propFindXmlGenerator", propFindXmlGenerator);
			}
			if (http11ResponseHandler == null) {
				http11ResponseHandler = new DefaultHttp11ResponseHandler(authenticationService, eTagGenerator);
				showLog("http11ResponseHandler", http11ResponseHandler);
			}
			webdavResponseHandler = new DefaultWebDavResponseHandler(http11ResponseHandler, resourceTypeHelper, propFindXmlGenerator);
			if (enableCompression) {
				webdavResponseHandler = new CompressingResponseHandler(webdavResponseHandler);
				showLog("webdavResponseHandler", webdavResponseHandler);
			}
			if (enableFormAuth) {
				log.info("form authentication is enabled, so wrap response handler with " + LoginResponseHandler.class);
				LoginResponseHandler loginResponseHandler = new LoginResponseHandler(webdavResponseHandler, mainResourceFactory);
				loginResponseHandler.setExcludePaths(loginPageExcludePaths);
				loginResponseHandler.setLoginPage(loginPage);
				webdavResponseHandler = loginResponseHandler;
			}
		}
		init(authenticationService, webdavResponseHandler, resourceTypeHelper);
	}

	private void init(AuthenticationService authenticationService, WebDavResponseHandler webdavResponseHandler, ResourceTypeHelper resourceTypeHelper) {
		initDone = true;
		if (handlerHelper == null) {
			handlerHelper = new HandlerHelper(authenticationService);
			showLog("handlerHelper", handlerHelper);
		}
		if (resourceHandlerHelper == null) {
			resourceHandlerHelper = new ResourceHandlerHelper(handlerHelper, urlAdapter, webdavResponseHandler);
			showLog("resourceHandlerHelper", resourceHandlerHelper);
		}
		if (protocols == null) {
			protocols = new ArrayList<HttpExtension>();
			Http11Protocol http11Protocol = new Http11Protocol(http11ResponseHandler, handlerHelper, resourceHandlerHelper, enableOptionsAuth);
			protocols.add(http11Protocol);
			if (propertySources == null) {
				propertySources = PropertySourceUtil.createDefaultSources(resourceTypeHelper);
				showLog("propertySources", propertySources);
			}
			if (extraPropertySources != null) {
				for (PropertySource ps : extraPropertySources) {
					log.info("Add extra property source: " + ps.getClass());
					propertySources.add(ps);
				}
			}
			if (propPatchSetter == null) {
				propPatchSetter = new PropertySourcePatchSetter(propertySources);
			}

			WebDavProtocol webDavProtocol = new WebDavProtocol(handlerHelper, resourceTypeHelper, webdavResponseHandler, propertySources, quotaDataAccessor, propPatchSetter, propertyAuthoriser, eTagGenerator, urlAdapter, resourceHandlerHelper);
			protocols.add(webDavProtocol);
			CalDavProtocol calDavProtocol = new CalDavProtocol(mainResourceFactory, webdavResponseHandler, handlerHelper, webDavProtocol);
			protocols.add(calDavProtocol);
			ACLProtocol acl = new ACLProtocol(webDavProtocol);
			protocols.add(acl);
			CardDavProtocol cardDavProtocol = new CardDavProtocol(mainResourceFactory, webdavResponseHandler, handlerHelper, webDavProtocol);
			protocols.add(cardDavProtocol);
		}

		if (protocolHandlers == null) {
			protocolHandlers = new ProtocolHandlers(protocols);
		}

		if (wellKnownHandlers == null) {
			wellKnownHandlers = new ArrayList<WellKnownResourceFactory.WellKnownHandler>();
			for (HttpExtension p : protocols) {
				if (p instanceof WellKnownResourceFactory.WellKnownHandler) {
					WellKnownResourceFactory.WellKnownHandler wellKnownHandler = (WellKnownResourceFactory.WellKnownHandler) p;
					wellKnownHandlers.add(wellKnownHandler);
				}
			}
		}

		// wrap the real (ie main) resource factory to provide well-known support and ajax gateway
		if (outerResourceFactory == null) {
			outerResourceFactory = mainResourceFactory; // in case nothing else enabled
			if (enabledJson) {
				outerResourceFactory = new JsonResourceFactory(outerResourceFactory, eventManager, propertySources, propPatchSetter, propertyAuthoriser);
			}
			if (enableWellKnown) {
				outerResourceFactory = new WellKnownResourceFactory(outerResourceFactory, wellKnownHandlers);
			}
		}
		if (filters != null) {
			filters = new ArrayList<Filter>(filters);
		} else {
			filters = new ArrayList<Filter>();
		}
		filters.add(defaultStandardFilter);
	}

	public HttpManager buildHttpManager() {
		if (!initDone) {
			init();
		}
		return new HttpManager(outerResourceFactory, webdavResponseHandler, protocolHandlers, entityTransport, filters, eventManager, shutdownHandlers);
	}

	public BUFFERING getBuffering() {
		return buffering;
	}

	public void setBuffering(BUFFERING buffering) {
		this.buffering = buffering;
	}

	public ResourceFactory getResourceFactory() {
		return mainResourceFactory;
	}

	public void setResourceFactory(ResourceFactory resourceFactory) {
		this.mainResourceFactory = resourceFactory;
	}

	public List<AuthenticationHandler> getAuthenticationHandlers() {
		return authenticationHandlers;
	}

	public void setAuthenticationHandlers(List<AuthenticationHandler> authenticationHandlers) {
		this.authenticationHandlers = authenticationHandlers;
	}

	/**
	 * Map holding nonce values issued in Digest authentication challenges
	 *
	 * @return
	 */
	public Map<UUID, Nonce> getNonces() {
		return nonces;
	}

	public void setNonces(Map<UUID, Nonce> nonces) {
		this.nonces = nonces;
	}

	/**
	 * This is your own resource factory, which provides access to your data
	 * repository. Not to be confused with outerResourceFactory which is
	 * normally used for milton specific things
	 *
	 * @return
	 */
	public ResourceFactory getMainResourceFactory() {
		return mainResourceFactory;
	}

	public void setMainResourceFactory(ResourceFactory mainResourceFactory) {
		this.mainResourceFactory = mainResourceFactory;
	}

	/**
	 * Usually set by milton, this will enhance the main resource factory with
	 * additional resources, such as .well-known support
	 *
	 * @return
	 */
	public ResourceFactory getOuterResourceFactory() {
		return outerResourceFactory;
	}

	public void setOuterResourceFactory(ResourceFactory outerResourceFactory) {
		this.outerResourceFactory = outerResourceFactory;
	}

	public int getNonceValiditySeconds() {
		return nonceValiditySeconds;
	}

	public void setNonceValiditySeconds(int nonceValiditySeconds) {
		this.nonceValiditySeconds = nonceValiditySeconds;
	}

	public NonceProvider getNonceProvider() {
		return nonceProvider;
	}

	public void setNonceProvider(NonceProvider nonceProvider) {
		this.nonceProvider = nonceProvider;
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public ExpiredNonceRemover getExpiredNonceRemover() {
		return expiredNonceRemover;
	}

	public void setExpiredNonceRemover(ExpiredNonceRemover expiredNonceRemover) {
		this.expiredNonceRemover = expiredNonceRemover;
	}

	public List<Stoppable> getShutdownHandlers() {
		return shutdownHandlers;
	}

	public void setShutdownHandlers(List<Stoppable> shutdownHandlers) {
		this.shutdownHandlers = shutdownHandlers;
	}

	public ResourceTypeHelper getResourceTypeHelper() {
		return resourceTypeHelper;
	}

	public void setResourceTypeHelper(ResourceTypeHelper resourceTypeHelper) {
		this.resourceTypeHelper = resourceTypeHelper;
	}

	public WebDavResponseHandler getWebdavResponseHandler() {
		return webdavResponseHandler;
	}

	public void setWebdavResponseHandler(WebDavResponseHandler webdavResponseHandler) {
		this.webdavResponseHandler = webdavResponseHandler;
	}

	public HandlerHelper getHandlerHelper() {
		return handlerHelper;
	}

	public void setHandlerHelper(HandlerHelper handlerHelper) {
		this.handlerHelper = handlerHelper;
	}

	public ArrayList<HttpExtension> getProtocols() {
		return protocols;
	}

	public void setProtocols(ArrayList<HttpExtension> protocols) {
		this.protocols = protocols;
	}

	public ProtocolHandlers getProtocolHandlers() {
		return protocolHandlers;
	}

	public void setProtocolHandlers(ProtocolHandlers protocolHandlers) {
		this.protocolHandlers = protocolHandlers;
	}

	public EntityTransport getEntityTransport() {
		return entityTransport;
	}

	public void setEntityTransport(EntityTransport entityTransport) {
		this.entityTransport = entityTransport;
	}

	public List<WellKnownResourceFactory.WellKnownHandler> getWellKnownHandlers() {
		return wellKnownHandlers;
	}

	public void setWellKnownHandlers(List<WellKnownResourceFactory.WellKnownHandler> wellKnownHandlers) {
		this.wellKnownHandlers = wellKnownHandlers;
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	public void setEventManager(EventManager eventManager) {
		this.eventManager = eventManager;
	}

	public PropertyAuthoriser getPropertyAuthoriser() {
		return propertyAuthoriser;
	}

	public void setPropertyAuthoriser(PropertyAuthoriser propertyAuthoriser) {
		this.propertyAuthoriser = propertyAuthoriser;
	}

	public List<PropertySource> getPropertySources() {
		return propertySources;
	}

	public void setPropertySources(List<PropertySource> propertySources) {
		this.propertySources = propertySources;
	}

	public ETagGenerator geteTagGenerator() {
		return eTagGenerator;
	}

	public void seteTagGenerator(ETagGenerator eTagGenerator) {
		this.eTagGenerator = eTagGenerator;
	}

	public Http11ResponseHandler getHttp11ResponseHandler() {
		return http11ResponseHandler;
	}

	public void setHttp11ResponseHandler(Http11ResponseHandler http11ResponseHandler) {
		this.http11ResponseHandler = http11ResponseHandler;
	}

	public ValueWriters getValueWriters() {
		return valueWriters;
	}

	public void setValueWriters(ValueWriters valueWriters) {
		this.valueWriters = valueWriters;
	}

	public PropFindXmlGenerator getPropFindXmlGenerator() {
		return propFindXmlGenerator;
	}

	public void setPropFindXmlGenerator(PropFindXmlGenerator propFindXmlGenerator) {
		this.propFindXmlGenerator = propFindXmlGenerator;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	public Filter getDefaultStandardFilter() {
		return defaultStandardFilter;
	}

	public void setDefaultStandardFilter(Filter defaultStandardFilter) {
		this.defaultStandardFilter = defaultStandardFilter;
	}

	public UrlAdapter getUrlAdapter() {
		return urlAdapter;
	}

	public void setUrlAdapter(UrlAdapter urlAdapter) {
		this.urlAdapter = urlAdapter;
	}

	public QuotaDataAccessor getQuotaDataAccessor() {
		return quotaDataAccessor;
	}

	public void setQuotaDataAccessor(QuotaDataAccessor quotaDataAccessor) {
		this.quotaDataAccessor = quotaDataAccessor;
	}

	public PropPatchSetter getPropPatchSetter() {
		return propPatchSetter;
	}

	public void setPropPatchSetter(PropPatchSetter propPatchSetter) {
		this.propPatchSetter = propPatchSetter;
	}

	public boolean isInitDone() {
		return initDone;
	}

	public void setInitDone(boolean initDone) {
		this.initDone = initDone;
	}

	/**
	 * False by default, which means that OPTIONS requests will not trigger
	 * authentication. This is required for windows 7
	 *
	 */
	public boolean isEnableOptionsAuth() {
		return enableOptionsAuth;
	}

	public void setEnableOptionsAuth(boolean enableOptionsAuth) {
		this.enableOptionsAuth = enableOptionsAuth;
	}

	public boolean isEnableCompression() {
		return enableCompression;
	}

	public void setEnableCompression(boolean enableCompression) {
		this.enableCompression = enableCompression;
	}

	public boolean isEnableWellKnown() {
		return enableWellKnown;
	}

	public void setEnableWellKnown(boolean enableWellKnown) {
		this.enableWellKnown = enableWellKnown;
	}

	public boolean isEnabledJson() {
		return enabledJson;
	}

	public void setEnabledJson(boolean enabledJson) {
		this.enabledJson = enabledJson;
	}

	public List<PropertySource> getExtraPropertySources() {
		return extraPropertySources;
	}

	public void setExtraPropertySources(List<PropertySource> extraPropertySources) {
		this.extraPropertySources = extraPropertySources;
	}

	/**
	 *
	 * @param propertyName
	 * @param defaultedTo
	 */
	private void showLog(String propertyName, Object defaultedTo) {
		log.info("set property: " + propertyName + " to: " + defaultedTo);
	}
}
