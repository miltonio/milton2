/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.milton.http.webdav;

import io.milton.common.NameSpace;
import io.milton.http.*;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.http.http11.CustomPostHandler;
import io.milton.http.http11.ETagGenerator;
import io.milton.http.quota.QuotaDataAccessor;
import io.milton.http.report.QualifiedReport;
import io.milton.http.report.Report;
import io.milton.http.report.ReportHandler;
import io.milton.http.values.SupportedReportSetList;
import io.milton.http.values.ValueWriters;
import io.milton.http.webdav.PropertyMap.StandardProperty;
import io.milton.http.webdav.PropertyMap.WritableStandardProperty;
import io.milton.property.PropertyAuthoriser;
import io.milton.property.PropertySource;
import io.milton.resource.CollectionResource;
import io.milton.resource.DisplayNameResource;
import io.milton.resource.GetableResource;
import io.milton.resource.PropFindableResource;
import io.milton.resource.PutableResource;
import io.milton.resource.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
 * generally doesnt include things defined in subsequent protocols (RFC's), but
 * where something is frequently used by other protocols (like REPORT) or is
 * very tightly couple with normal webdav operations (like quota checking)
 * you'll find it here
 *
 *
 * @author brad
 */
public class WebDavProtocol implements HttpExtension, PropertySource {

	private static final Logger log = LoggerFactory.getLogger(WebDavProtocol.class);
	public static final String DAV_URI = "DAV:";
	public static final String DAV_PREFIX = "d";
	public static final NameSpace NS_DAV = new NameSpace(DAV_URI, DAV_PREFIX);
	private final Set<Handler> handlers;
	private final Map<String, Report> reports;
	private final ResourceTypeHelper resourceTypeHelper;
	private final QuotaDataAccessor quotaDataAccessor;
	private final PropertyMap propertyMap;
	private final List<PropertySource> propertySources;
	private final ETagGenerator eTagGenerator;
	private final HandlerHelper handlerHelper;
	private final UserAgentHelper userAgentHelper;
	private final DisplayNameFormatter displayNameFormatter;
	private final MkColHandler mkColHandler;
	private final PropPatchHandler propPatchHandler;
	private List<CustomPostHandler> customPostHandlers;

	public WebDavProtocol(HandlerHelper handlerHelper, ResourceTypeHelper resourceTypeHelper, WebDavResponseHandler responseHandler, List<PropertySource> propertySources, QuotaDataAccessor quotaDataAccessor, PropPatchSetter patchSetter, PropertyAuthoriser propertyAuthoriser, ETagGenerator eTagGenerator, UrlAdapter urlAdapter, ResourceHandlerHelper resourceHandlerHelper, UserAgentHelper userAgentHelper, PropFindRequestFieldParser requestFieldParser, PropFindPropertyBuilder propertyBuilder, DisplayNameFormatter displayNameFormatter, boolean enableTextContentProperty) {
		this.displayNameFormatter = displayNameFormatter;
		this.userAgentHelper = userAgentHelper;
		this.handlerHelper = handlerHelper;
		this.eTagGenerator = eTagGenerator;
		handlers = new HashSet<Handler>();
		this.resourceTypeHelper = resourceTypeHelper;
		this.quotaDataAccessor = quotaDataAccessor;
		this.propertyMap = new PropertyMap(WebDavProtocol.NS_DAV.getName());

		propertyMap.add(new ContentLengthPropertyWriter());
		propertyMap.add(new ContentTypePropertyWriter());
		propertyMap.add(new CreationDatePropertyWriter("getcreated"));
		propertyMap.add(new CreationDatePropertyWriter("creationdate"));
		propertyMap.add(new DisplayNamePropertyWriter());
		propertyMap.add(new LastModifiedDatePropertyWriter());
		propertyMap.add(new ResourceTypePropertyWriter());
		propertyMap.add(new EtagPropertyWriter());

		propertyMap.add(new MSIsCollectionPropertyWriter());
		propertyMap.add(new MSIsReadOnlyPropertyWriter());
		propertyMap.add(new MSNamePropertyWriter());

		log.info("resourceTypeHelper: " + resourceTypeHelper.getClass());
		if (quotaDataAccessor == null) {
			log.info("no quota data");
		} else {
			log.info("quotaDataAccessor: " + quotaDataAccessor.getClass());
			propertyMap.add(new QuotaAvailableBytesPropertyWriter());
			propertyMap.add(new QuotaUsedBytesPropertyWriter());
		}

		propertyMap.add(new SupportedReportSetProperty());
		if (enableTextContentProperty) {
			propertyMap.add(new MiltonExtTextContentProperty());
		}

		// note valuewriters is also used in DefaultWebDavResponseHandler
		// if using non-default configuration you should inject the same instance into there
		// and here
		ValueWriters valueWriters = new ValueWriters();

		if (propertySources == null) {
			propertySources = new ArrayList<PropertySource>();
		}
		log.debug("provided property sources: " + propertySources.size());
		this.propertySources = propertySources;

		log.debug("adding webdav as a property source to: " + this.propertySources.getClass() + " hashCode: " + this.propertySources.hashCode());
		addPropertySource(this);
		if (patchSetter == null) {
			log.info("creating default patcheSetter: " + PropertySourcePatchSetter.class);
			patchSetter = new PropertySourcePatchSetter(propertySources, valueWriters);
		}
		//handlers.add(new PropFindHandler(resourceHandlerHelper, resourceTypeHelper, responseHandler, propertySources));
		PropFindHandler propFindHandler = new PropFindHandler(resourceHandlerHelper, requestFieldParser, responseHandler, propertyBuilder);
		handlers.add(propFindHandler);
		mkColHandler = new MkColHandler(responseHandler, handlerHelper);
		handlers.add(mkColHandler);
		propPatchHandler = new PropPatchHandler(resourceHandlerHelper, new DefaultPropPatchParser(), patchSetter, responseHandler, propertyAuthoriser);
		handlers.add(propPatchHandler);
		handlers.add(new CopyHandler(responseHandler, handlerHelper, resourceHandlerHelper, userAgentHelper));
		handlers.add(new MoveHandler(responseHandler, handlerHelper, resourceHandlerHelper, userAgentHelper));

		// Reports are added by other protocols via addReport
		reports = new HashMap<String, Report>();
		handlers.add(new ReportHandler(responseHandler, resourceHandlerHelper, reports));
	}

	@Override
	public List<CustomPostHandler> getCustomPostHandlers() {
		return customPostHandlers;
	}

	public void setCustomPostHandlers(List<CustomPostHandler> customPostHandlers) {
		this.customPostHandlers = customPostHandlers;
	}

	public List<PropertySource> getPropertySources() {
		return Collections.unmodifiableList(propertySources);
	}

	public void addPropertySource(PropertySource ps) {
		propertySources.add(ps);
		log.debug("adding property source: " + ps.getClass() + " new size: " + propertySources.size());
	}

	public void addReport(Report report) {
		this.reports.put(report.getName(), report);
	}

	@Override
	public Set<Handler> getHandlers() {
		return Collections.unmodifiableSet(handlers);
	}

	@Override
	public Object getProperty(QName name, Resource r) {
		Object o = propertyMap.getProperty(name, r);
		return o;
	}

	@Override
	public void setProperty(QName name, Object value, Resource r) {
		throw new UnsupportedOperationException("Not supported. Standard webdav properties are not writable");
	}

	@Override
	public PropertyMetaData getPropertyMetaData(QName name, Resource r) {
		PropertyMetaData propertyMetaData = propertyMap.getPropertyMetaData(name, r);
		if (propertyMetaData != null) {
			// Nautilus (at least on Ubuntu 12) doesnt like empty properties
			if (userAgentHelper.isNautilus(HttpManager.request())) {
				Object v = getProperty(name, r);
				if (v == null) {
					return PropertyMetaData.UNKNOWN;
				} else if (v instanceof String) {
					String s = (String) v;
					if (s.trim().length() == 0) {
						return PropertyMetaData.UNKNOWN;
					}
				}
			}
		}

		return propertyMetaData;
	}

	@Override
	public void clearProperty(QName name, Resource r) {
		throw new UnsupportedOperationException("Not supported. Standard webdav properties are not writable");
	}

	@Override
	public List<QName> getAllPropertyNames(Resource r) {
		return propertyMap.getAllPropertyNames(r);
	}

	/**
	 * Generates the displayname element text. By default is a
	 * CdataDisplayNameFormatter wrapping a DefaultDisplayNameFormatter so that
	 * extended character sets are supported
	 *
	 * @return
	 */
	public DisplayNameFormatter getDisplayNameFormatter() {
		return displayNameFormatter;
	}

	class DisplayNamePropertyWriter implements WritableStandardProperty<String> {

		@Override
		public String getValue(PropFindableResource res) {
			if( res instanceof DisplayNameResource) {
				DisplayNameResource dnr = (DisplayNameResource) res;
				return dnr.getDisplayName();
			}
			return displayNameFormatter.formatDisplayName(res);
		}

		@Override
		public String fieldName() {
			return "displayname";
		}

		@Override
		public Class<String> getValueClass() {
			return String.class;
		}

		@Override
		public void setValue(PropFindableResource res, String value) {
			if( res instanceof DisplayNameResource) {
				DisplayNameResource dnr = (DisplayNameResource) res;
				dnr.setDisplayName(value);
			} else {
				log.warn("Attempt to set displayname property, but resource is not compatible: " + res.getClass());
			}
		}
	}

	class CreationDatePropertyWriter implements StandardProperty<Date> {

		private final String fieldName;

		public CreationDatePropertyWriter(String fieldName) {
			this.fieldName = fieldName;
		}

		@Override
		public String fieldName() {
			return fieldName;
		}

		@Override
		public Date getValue(PropFindableResource res) {
			// BM: was getModifiedDate(), presume that was wrong??
			return res.getCreateDate();
		}

		@Override
		public Class<Date> getValueClass() {
			return Date.class;
		}
	}

	class LastModifiedDatePropertyWriter implements StandardProperty<Date> {

		@Override
		public String fieldName() {
			return "getlastmodified";
		}

		@Override
		public Date getValue(PropFindableResource res) {
			return res.getModifiedDate();
		}

		@Override
		public Class<Date> getValueClass() {
			return Date.class;
		}
	}

	class ResourceTypePropertyWriter implements StandardProperty<List<QName>> {

		@Override
		public List<QName> getValue(PropFindableResource res) {
			return resourceTypeHelper.getResourceTypes(res);
		}

		@Override
		public String fieldName() {
			return "resourcetype";
		}

		@Override
		public Class getValueClass() {
			return List.class;
		}
	}

	class ContentTypePropertyWriter implements StandardProperty<String> {

		@Override
		public String getValue(PropFindableResource res) {
			if (res instanceof GetableResource) {
				GetableResource getable = (GetableResource) res;
				String s = getable.getContentType(null);
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
		public Long getValue(PropFindableResource res) {
			if (res instanceof GetableResource) {
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
		public Long getValue(PropFindableResource res) {
			if (quotaDataAccessor != null) {
				return quotaDataAccessor.getQuotaUsed(res);
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

		@Override
		public Long getValue(PropFindableResource res) {
			if (quotaDataAccessor != null) {
				return quotaDataAccessor.getQuotaAvailable(res);
			} else {
				return null;
			}
		}

		@Override
		public String fieldName() {
			return "quota-available-bytes";
		}

		@Override
		public Class getValueClass() {
			return Long.class;
		}
	}

	class EtagPropertyWriter implements StandardProperty<String> {

		@Override
		public String getValue(PropFindableResource res) {
			String etag = eTagGenerator.generateEtag(res);
			return etag;
		}

		@Override
		public String fieldName() {
			return "getetag";
		}

		@Override
		public Class getValueClass() {
			return String.class;
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

		@Override
		public Boolean getValue(PropFindableResource res) {
			return (res instanceof CollectionResource);
		}

		@Override
		public Class getValueClass() {
			return Boolean.class;
		}
	}

	class MSIsReadOnlyPropertyWriter implements StandardProperty<Boolean> {

		@Override
		public String fieldName() {
			return "isreadonly";
		}

		@Override
		public Boolean getValue(PropFindableResource res) {
			return !(res instanceof PutableResource);
		}

		@Override
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
		public SupportedReportSetList getValue(PropFindableResource res) {
			SupportedReportSetList reportSet = new SupportedReportSetList();
			for (Report report: reports.values()) {
				if(report instanceof QualifiedReport)
					reportSet.add(((QualifiedReport) report).getQualifiedName());
				else
					reportSet.add(new QName(DAV_URI, report.getName()));
			}
			return reportSet;
		}

		@Override
		public Class getValueClass() {
			return SupportedReportSetList.class;
		}
	}

	class MiltonExtTextContentProperty implements StandardProperty<String> {

		@Override
		public String fieldName() {
			return "textcontent";
		}

		@Override
		public String getValue(PropFindableResource res) {
			if (res instanceof GetableResource) {
				GetableResource gr = (GetableResource) res;
				String ct = gr.getContentType("text");
				if (ct != null && ct.startsWith("text")) {
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					try {
						gr.sendContent(bout, null, Collections.EMPTY_MAP, ct);
						return bout.toString("UTF-8");
					} catch (IOException e) {
						throw new RuntimeException(e);
					} catch (NotAuthorizedException e) {
						return null;
					} catch (BadRequestException e) {
						return null;
					} catch (NotFoundException e) {
						return null;
					}
				}
			}
			return null;
		}

		@Override
		public Class getValueClass() {
			return String.class;
		}
	}

	protected void sendStringProp(XmlWriter writer, String name, String value) {
		String s = value;
		if (s == null) {
			writer.writeProperty(null, name);
		} else {
			writer.writeProperty(null, name, s);
		}
	}

	void sendDateProp(XmlWriter writer, String name, Date date) {
		sendStringProp(writer, name, (date == null ? null : DateUtils.formatDate(date)));
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
