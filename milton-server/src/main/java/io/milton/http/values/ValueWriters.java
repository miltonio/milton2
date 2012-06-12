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

package io.milton.http.values;

import io.milton.http.XmlWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default list of value writers. These are used to format strongly types
 * property values (eg Date, Boolean, Locks) into the appropriate XML
 *
 * They also parse string values in PROPPATCH requests into the strongly
 * typed values.
 *
 * @author brad
 */
public class ValueWriters {

	private static final Logger log = LoggerFactory.getLogger( ValueWriters.class );
	
    private final List<ValueWriter> writers;

    /**
     * Allows the set of value writers to be injected
     *
     * @param valueWriters
     */
    public ValueWriters(List<ValueWriter> valueWriters) {
        this.writers = valueWriters;
    }

    /**
     * Initialised the default set of writers
     *
     */
    public ValueWriters() {
        writers = new ArrayList<ValueWriter>();
        writers.add(new LockTokenValueWriter());
        writers.add(new SupportedLockValueWriter());

        // BM: Note that windows explorer is picky about its date format. This
        // property writer supports it explicitly
        writers.add(new ModifiedDateValueWriter());
        writers.add(new DateValueWriter());
        writers.add(new ResourceTypeValueWriter());
        writers.add(new BooleanValueWriter());
        writers.add(new CDataValueWriter());
        writers.add(new UUIDValueWriter());
        writers.add(new HrefListValueWriter());
        writers.add(new WrappedHrefWriter());
        writers.add(new SupportedReportSetWriter()); 
		writers.add(new AddressDataTypeListValueWriter());
		writers.add(new PriviledgeListValueWriter());
		// ToStringValueWriter is the default value writer and applied when no 
		// other writer is available.
		writers.add(new ToStringValueWriter()); 
    }

    /**
     * Find the first value writer which supports the given property and use it
     * to output the XML.
     *
     * @param writer
     * @param qname
     * @param prefix
     * @param vat
     * @param href
     * @param nsPrefixes
     */
    public void writeValue(XmlWriter writer, QName qname, String prefix, ValueAndType vat, String href, Map<String, String> nsPrefixes) {
        for (ValueWriter vw : writers) {
			if( vat.getValue() != null ) {
				if( vat.getValue().getClass() != vat.getType()) {
					throw new RuntimeException("Inconsistent type information: " + vat.getValue().getClass() + " != " + vat.getType());
				}
			}
            if (vw.supports(qname.getNamespaceURI(), qname.getLocalPart(), vat.getType())) {
                vw.writeValue(writer, qname.getNamespaceURI(), prefix, qname.getLocalPart(), vat.getValue(), href, nsPrefixes);
                break;
            }
        }
    }

    public List<ValueWriter> getValueWriters() {
        return writers;
    }

    /**
     * Find the first ValueWriter which supports the given property and use it
     * to parse the value
     *
     * @param qname
     * @param valueType
     * @param value
     * @return
     */
    public Object parse(QName qname, Class valueType, String value) {
        for (ValueWriter vw : writers) {
            if (vw.supports(qname.getNamespaceURI(), qname.getLocalPart(), valueType)) {
				log.trace("parse: Found supporting value writer {} ", vw);
                return vw.parse(qname.getNamespaceURI(), qname.getLocalPart(), value);
            }
        }
		log.warn("parse: No value writer supports: qname: " + qname + " type:" + valueType + " value:" + value );
        return null;
    }
}
