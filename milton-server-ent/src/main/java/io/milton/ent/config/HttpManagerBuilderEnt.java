/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */
package io.milton.ent.config;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.HttpExtension;
import io.milton.http.LockManager;
import io.milton.http.ProtocolHandlers;
import io.milton.http.WellKnownResourceFactory;
import io.milton.http.acl.ACLProtocol;
import io.milton.http.acl.AccessControlledResourceTypeHelper;
import io.milton.http.acl.AnnotationsPrincipalSearchService;
import io.milton.http.annotated.AnnotationResourceFactory;
import io.milton.http.caldav.AnnotationsCalendarSearchService;
import io.milton.http.caldav.CalDavProtocol;
import io.milton.http.caldav.CalendarResourceTypeHelper;
import io.milton.http.caldav.CalendarSearchService;
import io.milton.http.caldav.DefaultCalendarSearchService;
import io.milton.http.caldav.ICalFormatter;
import io.milton.http.caldav.SupportedCalendarComponentListValueWriter;
import io.milton.http.caldav.SupportedCalendarComponentListsSetValueWriter;
import io.milton.http.carddav.AddressBookResourceTypeHelper;
import io.milton.http.carddav.CardDavProtocol;
import io.milton.http.fck.FckResourceFactory;
import io.milton.http.fs.SimpleLockManager;
import io.milton.http.http11.Http11Protocol;
import io.milton.http.http11.MatchHelper;
import io.milton.http.http11.PartialGetHelper;
import io.milton.http.webdav.PropertySourcePatchSetter;
import io.milton.http.webdav.ResourceTypeHelper;
import io.milton.http.webdav.WebDavResourceTypeHelper;
import io.milton.http.webdav.WebDavResponseHandler;
import io.milton.http.webdav2.LockTokenValueWriter;
import io.milton.http.webdav2.SupportedLockValueWriter;
import io.milton.http.webdav2.WebDavLevel2Protocol;
import io.milton.http.webdav2.WebDavLevel2ResourceTypeHelper;
import io.milton.principal.PrincipalSearchService;
import io.milton.property.PropertySource;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Manages the options for configuring a HttpManager. To use it just set
 * properties on this class, then call init, then call buildHttpManager to get a
 * reference to the HttpManager.
 * </p>
 * <p>
 * Note that this uses a two-step construction process: init()
 * buildHttpManager()
 * </p>
 * <p>
 * The first step creates instances of any objects which have not been set and
 * the second binds them onto the HttpManager. You might want to modify the
 * objects created in the first step, eg setting properties on default
 * implementations. Note that you should not modify the structure of the
 * resultant object graph, because you could then end up with an inconsistent
 * configuration
 * </p>
 * <p>
 * Where possible, default implementations are created when this class is
 * constructed allowing them to be overwritten where needed. However this is
 * only done for objects and values which are "leaf" nodes in the config object
 * graph. This is to avoid inconsistent configuration where different parts of
 * milton end up with different implementations of the same concern. For
 * example, PropFind and PropPatch could end up using different property sources
 * </p>
 * 
 * @author brad
 */
public class HttpManagerBuilderEnt extends HttpManagerBuilder {

    private static final Logger log = LoggerFactory.getLogger(HttpManagerBuilderEnt.class);
    private List<WellKnownResourceFactory.WellKnownHandler> wellKnownHandlers;
    private CalDavProtocol calDavProtocol;
    private CardDavProtocol cardDavProtocol;
    private ACLProtocol aclProtocol;
    private boolean caldavEnabled = true;
    private boolean carddavEnabled = true;
    private boolean aclEnabled = true;
    private boolean enableWellKnown = true;
    private WebDavLevel2Protocol webDavLevel2Protocol;
    private boolean webdavLevel2Enabled = true;
    private LockManager lockManager;
    private ICalFormatter iCalFormatter;

    private CalendarSearchService calendarSearchService;
    private AnnotationsCalendarSearchService annotationsCalendarSearchService;

    private PrincipalSearchService principalSearchService;
    private AnnotationsPrincipalSearchService annotationsPrincipalSearchService;
    private WellKnownResourceFactory wellKnownResourceFactory;

    @Override
    protected void afterInit() {
        super.afterInit();
        if (getMainResourceFactory() instanceof AnnotationResourceFactory) {
            AnnotationResourceFactory arf = (AnnotationResourceFactory) getMainResourceFactory();

            if (arf.getLockManager() == null) {
                if (lockManager == null) {                    
                    lockManager = new SimpleLockManager(getCacheManager());
                    log.info("Created lock manager: {} with cache manager: {}",lockManager, getCacheManager());
                } else {
                    log.info("Using configured cache manager: {}", lockManager);
                }

                arf.setLockManager(lockManager);
            } else {
                log.info("Using LockManager from AnnotationResourceFactory: {}", arf.getLockManager().getClass());
            }
            if (annotationsCalendarSearchService != null) {
                annotationsCalendarSearchService.setAnnotationResourceFactory(arf);
            }
            arf.setCalendarSearchService(calendarSearchService);

            if (annotationsPrincipalSearchService != null) {
                annotationsPrincipalSearchService.setAnnotationResourceFactory(arf);
            }
        }
    }

    @Override
    protected void buildOuterResourceFactory() {
        log.info("buildOuterResourceFactory");
        // wrap the real (ie main) resource factory to provide well-known support and ajax gateway
        if (outerResourceFactory == null) {
            outerResourceFactory = mainResourceFactory; // in case nothing else enabled
            if (enabledJson) {
                outerResourceFactory = buildJsonResourceFactory();
                log.info("Enabled json/ajax gateway with: {}", outerResourceFactory.getClass());
            }

            if (enableWellKnown) {
                wellKnownResourceFactory = new WellKnownResourceFactory(outerResourceFactory);
                outerResourceFactory = wellKnownResourceFactory;
                // will set well known handlers after protocol init
                log.info("Enabled well-known protocol support with: {}", outerResourceFactory.getClass());
            }
            if (calendarSearchService == null) {
                log.warn("Using the default calendar search service. Calendar search functions may exhibit poor performance. If thats a problem implement your own: {}", CalendarSearchService.class);
                if( iCalFormatter == null ) {
                    iCalFormatter = new ICalFormatter();
                }
                DefaultCalendarSearchService c = new DefaultCalendarSearchService(iCalFormatter, mainResourceFactory);
                // Wrap the default in an annotations handler. It will forward requests to the wrapped
                // instance for non-annotation resources
                annotationsCalendarSearchService = new AnnotationsCalendarSearchService(c);
                calendarSearchService = annotationsCalendarSearchService;
            }
            if (principalSearchService == null) {
                annotationsPrincipalSearchService = new AnnotationsPrincipalSearchService();
                principalSearchService = annotationsPrincipalSearchService;
            }
            if (enabledCkBrowser) {
                outerResourceFactory = new FckResourceFactory(outerResourceFactory);
                log.info("Enabled CK Editor support with: " + outerResourceFactory.getClass());
            }
        }
    }

    @Override
    protected void buildResourceTypeHelper() {
        WebDavResourceTypeHelper webDavResourceTypeHelper = new WebDavResourceTypeHelper();
        WebDavLevel2ResourceTypeHelper davLevel2ResourceTypeHelper = new WebDavLevel2ResourceTypeHelper(webDavResourceTypeHelper);
        AccessControlledResourceTypeHelper accessControlledResourceTypeHelper = new AccessControlledResourceTypeHelper(davLevel2ResourceTypeHelper);
        CalendarResourceTypeHelper calendarResourceTypeHelper = new CalendarResourceTypeHelper(accessControlledResourceTypeHelper);
        resourceTypeHelper = new AddressBookResourceTypeHelper(calendarResourceTypeHelper);
    }

    @Override
    protected void buildProtocolHandlers(WebDavResponseHandler webdavResponseHandler, ResourceTypeHelper resourceTypeHelper) {
        if (protocols == null) {
            protocols = new ArrayList<>();

            if (matchHelper == null) {
                matchHelper = new MatchHelper(eTagGenerator);
            }
            if (partialGetHelper == null) {
                partialGetHelper = new PartialGetHelper();
            }

            Http11Protocol http11Protocol = new Http11Protocol(webdavResponseHandler, handlerHelper, resourceHandlerHelper, enableOptionsAuth, matchHelper, partialGetHelper);
            log.info("Enabled HTTP11 protocol");
            protocols.add(http11Protocol);

            initDefaultPropertySources(resourceTypeHelper);
            if (extraPropertySources != null) {
                for (PropertySource ps : extraPropertySources) {
                    log.info("Add extra property source: {}", ps.getClass());
                    propertySources.add(ps);
                }
            }
            if (propPatchSetter == null) {
                propPatchSetter = new PropertySourcePatchSetter(propertySources);
            }

            initWebdavProtocol();
            if (webDavProtocol != null) {
                log.info("Enabled DAV level 1 protocol");
                protocols.add(webDavProtocol);
            }

            if (webDavLevel2Protocol == null && webdavLevel2Enabled) {
                webDavLevel2Protocol = new WebDavLevel2Protocol(handlerHelper, webdavResponseHandler, resourceHandlerHelper, userAgentHelper());
            }
            if (webDavLevel2Protocol != null) {
                log.info("Enabled DAV level 2 protocol");
                valueWriters.getValueWriters().add(0, new SupportedLockValueWriter());
                valueWriters.getValueWriters().add(0, new LockTokenValueWriter());

                protocols.add(webDavLevel2Protocol);
                if (webDavProtocol != null) {
                    webDavProtocol.addPropertySource(webDavLevel2Protocol);
                }
            }

            if (calDavProtocol == null && caldavEnabled) {
                calDavProtocol = new CalDavProtocol(outerResourceFactory, webdavResponseHandler, handlerHelper, webDavProtocol, propFindXmlGenerator, propFindPropertyBuilder(), calendarSearchService);
            }
            if (calDavProtocol != null) {
                log.info("Add Caldav protocol: {} with resource factory: {}", calDavProtocol.getClass(), calDavProtocol.getResourceFactory());
                protocols.add(calDavProtocol);
            }

            if (aclProtocol == null && aclEnabled) {
                aclProtocol = new ACLProtocol(webDavProtocol, propFindPropertyBuilder(), propFindXmlGenerator, principalSearchService);
            }
            if (aclProtocol != null) {
                log.info("Enaled ACL Protocol");
                protocols.add(aclProtocol);
            }

            if (cardDavProtocol == null && carddavEnabled) {
                cardDavProtocol = new CardDavProtocol(mainResourceFactory, webdavResponseHandler, handlerHelper, webDavProtocol, propFindXmlGenerator, propFindPropertyBuilder());
            }
            if (cardDavProtocol != null) {
                valueWriters.getValueWriters().add(0, new SupportedCalendarComponentListValueWriter());
                valueWriters.getValueWriters().add(0, new SupportedCalendarComponentListsSetValueWriter());
                protocols.add(cardDavProtocol);
                log.info("Enabled CardDav protocol");
            }
        }

        if (protocolHandlers == null) {
            protocolHandlers = new ProtocolHandlers(protocols);
        }

        if (wellKnownResourceFactory != null) {
            log.info("Enabled well-known protocol");
            if (wellKnownHandlers == null) {
                wellKnownHandlers = new ArrayList<>();
                for (HttpExtension p : protocols) {
                    if (p instanceof WellKnownResourceFactory.WellKnownHandler) {
                        WellKnownResourceFactory.WellKnownHandler wellKnownHandler = (WellKnownResourceFactory.WellKnownHandler) p;
                        wellKnownHandlers.add(wellKnownHandler);
                    }
                }
            }
            wellKnownResourceFactory.setWellKnownHandlers(wellKnownHandlers);
        }
    }

    public List<WellKnownResourceFactory.WellKnownHandler> getWellKnownHandlers() {
        return wellKnownHandlers;
    }

    public void setWellKnownHandlers(List<WellKnownResourceFactory.WellKnownHandler> wellKnownHandlers) {
        this.wellKnownHandlers = wellKnownHandlers;
    }

    public boolean isEnableWellKnown() {
        return enableWellKnown;
    }

    public void setEnableWellKnown(boolean enableWellKnown) {
        this.enableWellKnown = enableWellKnown;
    }

    public CalDavProtocol getCalDavProtocol() {
        return calDavProtocol;
    }

    public void setCalDavProtocol(CalDavProtocol calDavProtocol) {
        this.calDavProtocol = calDavProtocol;
    }

    public CardDavProtocol getCardDavProtocol() {
        return cardDavProtocol;
    }

    public void setCardDavProtocol(CardDavProtocol cardDavProtocol) {
        this.cardDavProtocol = cardDavProtocol;
    }

    public ACLProtocol getAclProtocol() {
        return aclProtocol;
    }

    public void setAclProtocol(ACLProtocol aclProtocol) {
        this.aclProtocol = aclProtocol;
    }

    public boolean isAclEnabled() {
        return aclEnabled;
    }

    public void setAclEnabled(boolean aclEnabled) {
        this.aclEnabled = aclEnabled;
    }

    public boolean isCaldavEnabled() {
        return caldavEnabled;
    }

    public void setCaldavEnabled(boolean caldavEnabled) {
        this.caldavEnabled = caldavEnabled;
    }

    public boolean isCarddavEnabled() {
        return carddavEnabled;
    }

    public void setCarddavEnabled(boolean carddavEnabled) {
        this.carddavEnabled = carddavEnabled;
    }

    public WebDavLevel2Protocol getWebDavLevel2Protocol() {
        return webDavLevel2Protocol;
    }

    public void setWebDavLevel2Protocol(WebDavLevel2Protocol webDavLevel2Protocol) {
        this.webDavLevel2Protocol = webDavLevel2Protocol;
    }

    public boolean isWebdavLevel2Enabled() {
        return webdavLevel2Enabled;
    }

    public void setWebdavLevel2Enabled(boolean webdavLevel2Enabled) {
        this.webdavLevel2Enabled = webdavLevel2Enabled;
    }

    /**
     * Only required for AnnotationResourceFactory
     *
     * @return
     */
    public LockManager getLockManager() {
        return lockManager;
    }

    public void setLockManager(LockManager lockManager) {
        this.lockManager = lockManager;
    }

    /**
     * By default this wraps the default calendarsearchservice, and is only used
     * for resources which have appropriate annotations, otherwise processing
     * falls through to the default
     * 
     * @return 
     */
    public AnnotationsCalendarSearchService getAnnotationsCalendarSearchService() {
        return annotationsCalendarSearchService;
    }

    public void setAnnotationsCalendarSearchService(AnnotationsCalendarSearchService annotationsCalendarSearchService) {
        this.annotationsCalendarSearchService = annotationsCalendarSearchService;
    }

    /**
     * This is the instance that gets wired directly into the stack. You can set
     * this to prevent any default instances being created
     * 
     * @return 
     */
    public CalendarSearchService getCalendarSearchService() {
        return calendarSearchService;
    }

    public void setCalendarSearchService(CalendarSearchService calendarSearchService) {
        this.calendarSearchService = calendarSearchService;
    }

   /**
    * Search service used to satisfy principal property search reports. You can set this
    *  to prevent the default search service from being created.
    *
    * @return Principal search service
    */
    public PrincipalSearchService getPrincipalSearchService() {
       return principalSearchService;
    }

    public void setPrincipalSearchService(PrincipalSearchService principalSearchService) {
       this.principalSearchService = principalSearchService;
    }
       
}
