/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */
package io.milton.ent.config;

import io.milton.property.PropertySource;
import io.milton.config.HttpManagerBuilder;
import io.milton.http.*;
import io.milton.http.acl.ACLProtocol;
import io.milton.http.acl.AccessControlledResourceTypeHelper;
import io.milton.http.annotated.AnnotationResourceFactory;
import io.milton.http.caldav.CalDavProtocol;
import io.milton.http.caldav.CalendarResourceTypeHelper;
import io.milton.http.caldav.CalendarSearchService;
import io.milton.http.caldav.DefaultCalendarSearchService;
import io.milton.http.caldav.ICalFormatter;
import io.milton.http.caldav.SchedulingResourceFactory;
import io.milton.http.caldav.SupportedCalendarComponentListValueWriter;
import io.milton.http.caldav.SupportedCalendarComponentListsSetValueWriter;
import io.milton.http.carddav.AddressBookResourceTypeHelper;
import io.milton.http.carddav.CardDavProtocol;
import io.milton.http.fck.FckResourceFactory;
import io.milton.http.fs.SimpleLockManager;
import io.milton.http.http11.*;
import io.milton.http.webdav.DefaultPropFindPropertyBuilder;
import io.milton.http.webdav.PropertySourcePatchSetter;
import io.milton.http.webdav.ResourceTypeHelper;
import io.milton.http.webdav.WebDavResourceTypeHelper;
import io.milton.http.webdav.WebDavResponseHandler;
import io.milton.http.webdav2.LockTokenValueWriter;
import io.milton.http.webdav2.SupportedLockValueWriter;
import io.milton.http.webdav2.WebDavLevel2Protocol;
import io.milton.http.webdav2.WebDavLevel2ResourceTypeHelper;
import java.util.ArrayList;
import java.util.List;
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
    private boolean enableScheduling = true;
    private WebDavLevel2Protocol webDavLevel2Protocol;
    private boolean webdavLevel2Enabled = true;
    private LockManager lockManager = new SimpleLockManager();
    private ICalFormatter iCalFormatter = new ICalFormatter();
    private CalendarSearchService calendarSearchService;
    private WellKnownResourceFactory wellKnownResourceFactory;

    @Override
    protected void afterInit() {
        super.afterInit();
        if (getMainResourceFactory() instanceof AnnotationResourceFactory) {
            AnnotationResourceFactory arf = (AnnotationResourceFactory) getMainResourceFactory();
            if (arf.getLockManager() == null) {
                arf.setLockManager(lockManager);
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
                log.info("Enabled json/ajax gatewayw with: " + outerResourceFactory.getClass());
            }

            if (enableWellKnown) {
                wellKnownResourceFactory = new WellKnownResourceFactory(outerResourceFactory);
                outerResourceFactory = wellKnownResourceFactory;
                // will set well known handlers after protocol init
                log.info("Enabled well-known protocol support with: " + outerResourceFactory.getClass());
            }
            if (calendarSearchService == null) {
                DefaultCalendarSearchService c = new DefaultCalendarSearchService(iCalFormatter, mainResourceFactory);
                c.setSchedulingEnabled(enableScheduling);
                calendarSearchService = c;
            }            
            if (calendarSearchService.isSchedulingEnabled()) {
                outerResourceFactory = new SchedulingResourceFactory(outerResourceFactory, calendarSearchService);
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
            protocols = new ArrayList<HttpExtension>();

            if (matchHelper == null) {
                matchHelper = new MatchHelper(eTagGenerator);
            }
            if (partialGetHelper == null) {
                partialGetHelper = new PartialGetHelper(webdavResponseHandler);
            }

            Http11Protocol http11Protocol = new Http11Protocol(webdavResponseHandler, handlerHelper, resourceHandlerHelper, enableOptionsAuth, matchHelper, partialGetHelper);
            protocols.add(http11Protocol);

                
            initDefaultPropertySources(resourceTypeHelper);
            if (extraPropertySources != null) {
                for (PropertySource ps : extraPropertySources) {
                    log.info("Add extra property source: " + ps.getClass());
                    propertySources.add(ps);
                }
            }
            if (propPatchSetter == null) {
                propPatchSetter = new PropertySourcePatchSetter(propertySources);
            }


            initWebdavProtocol();
            if (webDavProtocol != null) {
                protocols.add(webDavProtocol);
            }

            if (webDavLevel2Protocol == null && webdavLevel2Enabled) {
                webDavLevel2Protocol = new WebDavLevel2Protocol(handlerHelper, webdavResponseHandler, resourceHandlerHelper, userAgentHelper());
            }
            if (webDavLevel2Protocol != null) {
                valueWriters.getValueWriters().add(0, new SupportedLockValueWriter());
                valueWriters.getValueWriters().add(0, new LockTokenValueWriter());

                protocols.add(webDavLevel2Protocol);
                if (webDavProtocol != null) {
                    webDavProtocol.addPropertySource(webDavLevel2Protocol);
                }
            }

            if (calDavProtocol == null && caldavEnabled) {
                System.out.println("configure caldavprotocol with outer resource factory: " + outerResourceFactory);
                calDavProtocol = new CalDavProtocol(outerResourceFactory, webdavResponseHandler, handlerHelper, webDavProtocol, propFindXmlGenerator, propFindPropertyBuilder(), calendarSearchService);
            }
            if (calDavProtocol != null) {
                protocols.add(calDavProtocol);
            }

            if (aclProtocol == null && aclEnabled) {
                aclProtocol = new ACLProtocol(webDavProtocol);
            }
            if (aclProtocol != null) {
                protocols.add(aclProtocol);
            }

            if (cardDavProtocol == null && carddavEnabled) {
                cardDavProtocol = new CardDavProtocol(mainResourceFactory, webdavResponseHandler, handlerHelper, webDavProtocol, propFindXmlGenerator, propFindPropertyBuilder());
            }
            if (cardDavProtocol != null) {
                valueWriters.getValueWriters().add(0, new SupportedCalendarComponentListValueWriter());
                valueWriters.getValueWriters().add(0, new SupportedCalendarComponentListsSetValueWriter());
                protocols.add(cardDavProtocol);
            }
        }

        if (protocolHandlers == null) {
            protocolHandlers = new ProtocolHandlers(protocols);
        }

        if (wellKnownResourceFactory != null) {
            if (wellKnownHandlers == null) {
                wellKnownHandlers = new ArrayList<WellKnownResourceFactory.WellKnownHandler>();
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
}
