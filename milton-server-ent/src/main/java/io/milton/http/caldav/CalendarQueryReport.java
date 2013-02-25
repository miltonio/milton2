/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */

package io.milton.http.caldav;

import io.milton.http.DateUtils;
import io.milton.http.DateUtils.DateParseException;
import io.milton.http.HttpManager;
import io.milton.common.Utils;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.webdav.PropFindPropertyBuilder;
import io.milton.http.webdav.PropFindResponse;
import io.milton.http.webdav.PropFindXmlGenerator;
import io.milton.http.webdav.PropertiesRequest;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.resource.CalendarResource;
import io.milton.resource.ICalResource;
import io.milton.http.report.Report;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class CalendarQueryReport implements Report {

	private static final Logger log = LoggerFactory.getLogger(CalendarQueryReport.class);
	private final PropFindPropertyBuilder propertyBuilder;
	private final PropFindXmlGenerator xmlGenerator;
	private final Namespace NS_DAV = Namespace.getNamespace(WebDavProtocol.NS_DAV.getPrefix(), WebDavProtocol.NS_DAV.getName());
	private final Namespace NS_CAL = Namespace.getNamespace("C", CalDavProtocol.CALDAV_NS);
	private final ICalFormatter formatter = new ICalFormatter();

	public CalendarQueryReport(PropFindPropertyBuilder propertyBuilder, PropFindXmlGenerator xmlGenerator) {
		this.propertyBuilder = propertyBuilder;
		this.xmlGenerator = xmlGenerator;
	}

	@Override
	public String getName() {
		return "calendar-query";
	}

	@Override
	public String process(String host, String path, Resource resource, Document doc) throws BadRequestException, NotAuthorizedException {
		log.debug("process");
		// The requested properties
		Set<QName> props = getProps(doc);

		PropertiesRequest parseResult = PropertiesRequest.toProperties(props);

		// Generate the response
		List<PropFindResponse> respProps = new ArrayList<PropFindResponse>();

		if (resource instanceof CalendarResource) {
			CalendarResource calendar = (CalendarResource) resource;
			List<ICalResource> foundResources = findCalendarResources(calendar, doc);
			log.trace("foundResources: " + foundResources.size());
			String parentHref = HttpManager.request().getAbsolutePath();
			parentHref = Utils.suffixSlash(parentHref);
			for (ICalResource cr : foundResources) {
				String href = parentHref + cr.getName();
				//List<PropFindResponse> resps = propertyBuilder.buildProperties(calendar, 0, parseResult, href);

				List<PropFindResponse> resps = new ArrayList<PropFindResponse>();
				propertyBuilder.processResource(resps, cr, parseResult, href, 0, 0, href);

				respProps.addAll(resps);
			}
		} else {
			throw new BadRequestException(resource, "Resource is not a " + CalendarResource.class.getCanonicalName() + " is a: " + resource.getClass());
		}

		String xml = xmlGenerator.generate(respProps);
		return xml;
	}

	private Set<QName> getProps(Document doc) {
		Element elProp = doc.getRootElement().getChild("prop", NS_DAV);
		if (elProp == null) {
			throw new RuntimeException("No prop element");
		}

		Set<QName> set = new HashSet<QName>();
		for (Object o : elProp.getChildren()) {
			if (o instanceof Element) {
				Element el = (Element) o;
				String local = el.getName();
				String ns = el.getNamespaceURI();
				set.add(new QName(ns, local, el.getNamespacePrefix()));
			}
		}
		return set;
	}

	private List<ICalResource> findCalendarResources(CalendarResource calendar, Document doc) throws NotAuthorizedException, BadRequestException {
		// build a list of all calendar resources
		List<ICalResource> list = new ArrayList<ICalResource>();
		for (Resource r : calendar.getChildren()) {
			if (r instanceof ICalResource) {
				ICalResource cr = (ICalResource) r;
				list.add(cr);
			}
		}

		// filter out those that don't match
		Element elFilterRoot = doc.getRootElement().getChild("filter", NS_CAL);
		if (elFilterRoot == null) {
			// no filter so return all
			return list;
		}

		Element elSecondFilter = elFilterRoot.getChild("comp-filter", NS_CAL);
		if (elSecondFilter == null) {
			// no second filter so return all
			return list;
		}

		Element elTimeRange = elSecondFilter.getChild("time-range", NS_CAL);
		if (elTimeRange == null) {
			// no time range filter so return all
			return list;
		}

		String sStart = elTimeRange.getAttributeValue("start");
		String sFinish = elTimeRange.getAttributeValue("end");

		Date start = null;
		Date end = null;

		if (sStart != null && sStart.length() > 0) {
			try {
				start = DateUtils.parseDate(sStart);
			} catch (DateParseException ex) {
				log.error("Couldnt parse start date in calendar-query: " + sStart);
			}
		}

		if (sFinish != null && sFinish.length() > 0) {
			try {
				end = DateUtils.parseDate(sFinish);
			} catch (DateParseException ex) {
				log.error("Couldnt parse end date in calendar-query: " + sFinish);
			}
		}

		// So now we have (or might have) start and end dates, so filter list
		Iterator<ICalResource> it = list.iterator();
		while (it.hasNext()) {
			ICalResource r = it.next();
			if (outsideDates(r, start, end)) {
				it.remove();
			}
		}
		return list;
	}

	private boolean outsideDates(ICalResource r, Date start, Date end) {
		EventResource data;
		if (r instanceof EventResource) {
			data = (EventResource) r;
		} else {
			data = new EventResourceImpl();
			formatter.parseEvent(data, r.getICalData());
		}

		if (start != null) {
			if (data.getStart().before(start)) {
				return true;
			}
		}

		if (end != null) {
			if (data.getEnd().after(end)) {
				return true;
			}
		}

		return false;
	}
}
