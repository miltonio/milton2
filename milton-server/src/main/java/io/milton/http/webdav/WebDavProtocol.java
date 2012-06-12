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

package io.milton.http.webdav;

import io.milton.common.NameSpace;
import io.milton.resource.CollectionResource;
import io.milton.http.DateUtils;
import io.milton.resource.GetableResource;
import io.milton.http.http11.CustomPostHandler;
import io.milton.property.PropertySource;
import io.milton.http.Handler;
import io.milton.http.HandlerHelper;
import io.milton.http.HttpExtension;
import io.milton.http.LockToken;
import io.milton.resource.LockableResource;
import io.milton.resource.PropFindableResource;
import io.milton.resource.PutableResource;
import io.milton.resource.Resource;
import io.milton.http.ResourceHandlerHelper;
import io.milton.http.XmlWriter;
import io.milton.http.http11.DefaultETagGenerator;
import io.milton.http.http11.ETagGenerator;
import io.milton.http.quota.DefaultQuotaDataAccessor;
import io.milton.http.quota.QuotaDataAccessor;
import io.milton.http.values.SupportedReportSetList;
import io.milton.http.values.ValueWriters;
import io.milton.http.webdav.PropertyMap.StandardProperty;
import io.milton.http.report.Report;
import io.milton.http.report.ReportHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the methods and properties that make up the webdav protocol.
 *
 * We've been a little pragmatic about what the webdav protocol actually is. It
 * generally doesnt include things defined in subsequent protocols (RFC's), but where
 * something is frequently used by other protocols (like REPORT) or is very tightly
 * couple with normal webdav operations (like quota checking) you'll find it here
 * 
 *
 * @author brad
 */
public class WebDavProtocol implements HttpExtension, PropertySource {

    private static final Logger log = LoggerFactory.getLogger( WebDavProtocol.class );
    public static final String DAV_URI = "DAV:";
	public static final String DAV_PREFIX = "d";
    public static final NameSpace NS_DAV = new NameSpace(DAV_URI, DAV_PREFIX );
    private final Set<Handler> handlers;
    private final Map<String, Report> reports;
    private final ResourceTypeHelper resourceTypeHelper;
    private final QuotaDataAccessor quotaDataAccessor;
    private final PropertyMap propertyMap;
    private final List<PropertySource> propertySources;
    private final ETagGenerator eTagGenerator;
    private final HandlerHelper handlerHelper;
    private DisplayNameFormatter displayNameFormatter = new DefaultDisplayNameFormatter();

    private final MkColHandler mkColHandler;
    private final PropPatchHandler propPatchHandler;


    private List<CustomPostHandler> customPostHandlers;
    //private DisplayNameFormatter displayNameFormatter = new CdataDisplayNameFormatter( new DefaultDisplayNameFormatter());

//    public WebDavProtocol( Set<Handler> handlers ) {
//        this.handlers = handlers;
//        reports = new HashMap<String, Report>();
//    }
    public WebDavProtocol( WebDavResponseHandler responseHandler, HandlerHelper handlerHelper ) {
        this( responseHandler, handlerHelper, new WebDavResourceTypeHelper() );
    }

    public WebDavProtocol( WebDavResponseHandler responseHandler, HandlerHelper handlerHelper, ResourceTypeHelper resourceTypeHelper ) {
        this( handlerHelper, resourceTypeHelper, responseHandler, PropertySourceUtil.createDefaultSources( resourceTypeHelper ) );
    }

    public WebDavProtocol( HandlerHelper handlerHelper, ResourceTypeHelper resourceTypeHelper, WebDavResponseHandler responseHandler, List<PropertySource> extraPropertySources ) {
        this( handlerHelper, resourceTypeHelper, responseHandler, extraPropertySources, new DefaultQuotaDataAccessor() );
    }

    public WebDavProtocol( HandlerHelper handlerHelper, ResourceTypeHelper resourceTypeHelper, WebDavResponseHandler responseHandler, List<PropertySource> extraPropertySources, QuotaDataAccessor quotaDataAccessor ) {
        this( handlerHelper, resourceTypeHelper, responseHandler, extraPropertySources, quotaDataAccessor, null );
    }

    public WebDavProtocol( HandlerHelper handlerHelper, ResourceTypeHelper resourceTypeHelper, WebDavResponseHandler responseHandler, List<PropertySource> propertySources, QuotaDataAccessor quotaDataAccessor, PropPatchSetter patchSetter ) {
        this.handlerHelper = handlerHelper;
        this.eTagGenerator = new DefaultETagGenerator();
        handlers = new HashSet<Handler>();
        this.resourceTypeHelper = resourceTypeHelper;
        this.quotaDataAccessor = quotaDataAccessor;
        this.propertyMap = new PropertyMap( WebDavProtocol.NS_DAV.getName() );

        log.info( "resourceTypeHelper: " + resourceTypeHelper.getClass() );
        if( quotaDataAccessor == null ) {
            log.info("no quota data");
        } else {
            log.info( "quotaDataAccessor: " + quotaDataAccessor.getClass() );
        }
        propertyMap.add( new ContentLengthPropertyWriter() );
        propertyMap.add( new ContentTypePropertyWriter() );
        propertyMap.add( new CreationDatePropertyWriter( "getcreated" ) );
        propertyMap.add( new CreationDatePropertyWriter( "creationdate" ) );
        propertyMap.add( new DisplayNamePropertyWriter() );
        propertyMap.add( new LastModifiedDatePropertyWriter() );
        propertyMap.add( new ResourceTypePropertyWriter() );
        propertyMap.add( new EtagPropertyWriter() );

        propertyMap.add( new SupportedLockPropertyWriter() );
        propertyMap.add( new LockDiscoveryPropertyWriter() );

        propertyMap.add( new MSIsCollectionPropertyWriter() );
        propertyMap.add( new MSIsReadOnlyPropertyWriter() );
        propertyMap.add( new MSNamePropertyWriter() );

        propertyMap.add( new QuotaAvailableBytesPropertyWriter() );
        propertyMap.add( new QuotaUsedBytesPropertyWriter() );

        propertyMap.add( new SupportedReportSetProperty() );

        ResourceHandlerHelper resourceHandlerHelper = new ResourceHandlerHelper( handlerHelper, responseHandler );

        // note valuewriters is also used in DefaultWebDavResponseHandler
        // if using non-default configuration you should inject the same instance into there
        // and here
        ValueWriters valueWriters = new ValueWriters();

		if(propertySources == null ) {
			propertySources = new ArrayList<PropertySource>();
		}
		log.debug( "provided property sources: " + propertySources.size() );
        this.propertySources = propertySources;

        log.debug( "adding webdav as a property source to: " + this.propertySources.getClass() + " hashCode: " + this.propertySources.hashCode() );
        addPropertySource( this );
        if( patchSetter == null ) {
            log.info( "creating default patcheSetter: " + PropertySourcePatchSetter.class );
            patchSetter = new PropertySourcePatchSetter( propertySources, valueWriters );
        }
        handlers.add( new PropFindHandler( resourceHandlerHelper, resourceTypeHelper, responseHandler, propertySources ) );
        mkColHandler = new MkColHandler( responseHandler, handlerHelper );
        handlers.add( mkColHandler );
        propPatchHandler = new PropPatchHandler( resourceHandlerHelper, responseHandler, patchSetter );
        handlers.add( propPatchHandler );
        handlers.add( new CopyHandler( responseHandler, handlerHelper, resourceHandlerHelper ) );
        handlers.add( new LockHandler( responseHandler, handlerHelper ) );
        handlers.add( new UnlockHandler( resourceHandlerHelper, responseHandler ) );
        handlers.add( new MoveHandler( responseHandler, handlerHelper, resourceHandlerHelper ) );

        // Reports are added by other protocols via addReport
        reports = new HashMap<String, Report>();
        handlers.add( new ReportHandler( responseHandler, resourceHandlerHelper, reports ) );
    }

	@Override
    public List<CustomPostHandler> getCustomPostHandlers() {
        return customPostHandlers;
    }

	public void setCustomPostHandlers(List<CustomPostHandler> customPostHandlers) {
		this.customPostHandlers = customPostHandlers;
	}
	
	

    public List<PropertySource> getPropertySources() {
        return Collections.unmodifiableList( propertySources );
    }

    public void addPropertySource( PropertySource ps ) {
        propertySources.add( ps );
        log.debug( "adding property source: " + ps.getClass() + " new size: " + propertySources.size() );
    }

    public void addReport( Report report ) {
        this.reports.put( report.getName(), report );
    }

	@Override
    public Set<Handler> getHandlers() {
        return Collections.unmodifiableSet( handlers );
    }



    /**
     * Used as a marker to generate supported locks element in propfind responses
     *
     * See SupportedLockValueWriter
     */
    public static class SupportedLocks {
		private final PropFindableResource res;

		public SupportedLocks(PropFindableResource res) {
			this.res = res;
		}

		public PropFindableResource getResource() {
			return res;
		}				
    }

	@Override
    public Object getProperty( QName name, Resource r ) {
        Object o = propertyMap.getProperty( name, r );
        return o;
    }

	@Override
    public void setProperty( QName name, Object value, Resource r ) {
        throw new UnsupportedOperationException( "Not supported. Standard webdav properties are not writable" );
    }

	@Override
    public PropertyMetaData getPropertyMetaData( QName name, Resource r ) {
        PropertyMetaData propertyMetaData = propertyMap.getPropertyMetaData( name, r );
        return propertyMetaData;
    }

	@Override
    public void clearProperty( QName name, Resource r ) {
        throw new UnsupportedOperationException( "Not supported. Standard webdav properties are not writable" );
    }

	@Override
    public List<QName> getAllPropertyNames( Resource r ) {
        return propertyMap.getAllPropertyNames( r );
    }

    /**
     * Generates the displayname element text. By default is a CdataDisplayNameFormatter
     * wrapping a DefaultDisplayNameFormatter so that extended character sets
     * are supported
     *
     * @return
     */
    public DisplayNameFormatter getDisplayNameFormatter() {
        return displayNameFormatter;
    }

    public void setDisplayNameFormatter( DisplayNameFormatter displayNameFormatter ) {
        this.displayNameFormatter = displayNameFormatter;
    }

    class DisplayNamePropertyWriter implements StandardProperty<String> {

        public String getValue( PropFindableResource res ) {
            return displayNameFormatter.formatDisplayName( res );
        }

        public String fieldName() {
            return "displayname";
        }

        public Class<String> getValueClass() {
            return String.class;
        }
    }

    class CreationDatePropertyWriter implements StandardProperty<Date> {

        private final String fieldName;

        public CreationDatePropertyWriter( String fieldName ) {
            this.fieldName = fieldName;
        }

        public String fieldName() {
            return fieldName;
        }

        public Date getValue( PropFindableResource res ) {
            // BM: was getModifiedDate(), presume that was wrong??
            return res.getCreateDate();
        }

        public Class<Date> getValueClass() {
            return Date.class;
        }
    }

    class LastModifiedDatePropertyWriter implements StandardProperty<Date> {

        public String fieldName() {
            return "getlastmodified";
        }

        public Date getValue( PropFindableResource res ) {
            return res.getModifiedDate();
        }

        public Class<Date> getValueClass() {
            return Date.class;
        }
    }

    class ResourceTypePropertyWriter implements StandardProperty<List<QName>> {

        public List<QName> getValue( PropFindableResource res ) {
            log.trace( "ResourceTypePropertyWriter:getValue" );
            return resourceTypeHelper.getResourceTypes( res );
        }

        public String fieldName() {
            return "resourcetype";
        }

        public Class getValueClass() {
            return List.class;
        }
    }

    class ContentTypePropertyWriter implements StandardProperty<String> {

        public String getValue( PropFindableResource res ) {
            if( res instanceof GetableResource ) {
                GetableResource getable = (GetableResource) res;
                String s = getable.getContentType( null );
				return s;
            } else {
                return "";
            }
        }

		@Override
        public String fieldName() {
            return "getcontenttype";
        }

		@Override
        public Class getValueClass() {
            return String.class;
        }
    }

    class ContentLengthPropertyWriter implements StandardProperty<Long> {

		@Override
        public Long getValue( PropFindableResource res ) {
            if( res instanceof GetableResource ) {
                GetableResource getable = (GetableResource) res;
                Long l = getable.getContentLength();
                return l;
            } else {
                return null;
            }
        }

		@Override
        public String fieldName() {
            return "getcontentlength";
        }

		@Override
        public Class getValueClass() {
            return Long.class;
        }
    }

    class QuotaUsedBytesPropertyWriter implements StandardProperty<Long> {

		@Override
        public Long getValue( PropFindableResource res ) {
			if( quotaDataAccessor != null ) {
				return quotaDataAccessor.getQuotaUsed( res );
			} else {
				return null;
			}
        }

		@Override
        public String fieldName() {
            return "quota-used-bytes";
        }

		@Override
        public Class getValueClass() {
            return Long.class;
        }
    }

    class QuotaAvailableBytesPropertyWriter implements StandardProperty<Long> {

        public Long getValue( PropFindableResource res ) {
			if( quotaDataAccessor != null ) {
				return quotaDataAccessor.getQuotaAvailable( res );
			} else {
				return null;
			}
        }

        public String fieldName() {
            return "quota-available-bytes";
        }

        public Class getValueClass() {
            return Long.class;
        }
    }

    class EtagPropertyWriter implements StandardProperty<String> {

        public String getValue( PropFindableResource res ) {
            String etag = eTagGenerator.generateEtag( res );
            return etag;
        }

        public String fieldName() {
            return "getetag";
        }

        public Class getValueClass() {
            return String.class;
        }
    }

//    <D:supportedlock/><D:lockdiscovery/>
    class LockDiscoveryPropertyWriter implements StandardProperty<LockToken> {

        public LockToken getValue( PropFindableResource res ) {
            if( !( res instanceof LockableResource ) ) return null;
            LockableResource lr = (LockableResource) res;
            LockToken token = lr.getCurrentLock();
            return token;
        }

        public String fieldName() {
            return "lockdiscovery";
        }

        public Class getValueClass() {
            return LockToken.class;
        }
    }

    class SupportedLockPropertyWriter implements StandardProperty<SupportedLocks> {

        public SupportedLocks getValue( PropFindableResource res ) {
            if( res instanceof LockableResource ) {
                return new SupportedLocks(res);
            } else {
                return null;
            }
        }

        public String fieldName() {
            return "supportedlock";
        }

        public Class getValueClass() {
            return SupportedLocks.class;
        }
    }

    // MS specific fields
    class MSNamePropertyWriter extends DisplayNamePropertyWriter {

        @Override
        public String fieldName() {
            return "name";
        }
    }

    class MSIsCollectionPropertyWriter implements StandardProperty<Boolean> {

        @Override
        public String fieldName() {
            return "iscollection";
        }

        public Boolean getValue( PropFindableResource res ) {
            return ( res instanceof CollectionResource );
        }

        public Class getValueClass() {
            return Boolean.class;
        }
    }

    class MSIsReadOnlyPropertyWriter implements StandardProperty<Boolean> {

        @Override
        public String fieldName() {
            return "isreadonly";
        }

        public Boolean getValue( PropFindableResource res ) {
            return !( res instanceof PutableResource );
        }

        public Class getValueClass() {
            return Boolean.class;
        }
    }

    class SupportedReportSetProperty implements StandardProperty<SupportedReportSetList> {

		@Override
        public String fieldName() {
            return "supported-report-set";
        }

		@Override
        public SupportedReportSetList getValue( PropFindableResource res ) {
            SupportedReportSetList reportSet = new SupportedReportSetList();
            for( String reportName : reports.keySet() ) {
                reportSet.add( reportName );
            }
            return reportSet;
        }

		@Override
        public Class getValueClass() {
            return SupportedReportSetList.class;
        }
    }

    protected void sendStringProp( XmlWriter writer, String name, String value ) {
        String s = value;
        if( s == null ) {
            writer.writeProperty( null, name );
        } else {
            writer.writeProperty( null, name, s );
        }
    }

    void sendDateProp( XmlWriter writer, String name, Date date ) {
        sendStringProp( writer, name, ( date == null ? null : DateUtils.formatDate( date ) ) );
    }

    public HandlerHelper getHandlerHelper() {
        return handlerHelper;
    }

    public QuotaDataAccessor getQuotaDataAccessor() {
        return quotaDataAccessor;
    }

    public Map<String, Report> getReports() {
        return reports;
    }

    public ResourceTypeHelper getResourceTypeHelper() {
        return resourceTypeHelper;
    }

    public ETagGenerator geteTagGenerator() {
        return eTagGenerator;
    }

    public PropertyMap getPropertyMap() {
        return propertyMap;
    }

    public MkColHandler getMkColHandler() {
        return mkColHandler;
    }

    public PropPatchHandler getPropPatchHandler() {
        return propPatchHandler;
    }

    
}
