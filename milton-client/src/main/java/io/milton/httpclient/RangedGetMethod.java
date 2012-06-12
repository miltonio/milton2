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

package io.milton.httpclient;

import io.milton.http.Range;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the HTTP GET method.
 * <p>
 * The HTTP GET method is defined in section 9.3 of
 * <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC2616</a>:
 * <blockquote>
 * The GET method means retrieve whatever information (in the form of an
 * entity) is identified by the Request-URI. If the Request-URI refers
 * to a data-producing process, it is the produced data which shall be
 * returned as the entity in the response and not the source text of the
 * process, unless that text happens to be the output of the process.
 * </blockquote>
 * </p>
 * <p>
 * GetMethods will follow redirect requests from the http server by default.
 * This behavour can be disabled by calling setFollowRedirects(false).</p>
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author Sung-Gu Park
 * @author Sean C. Sullivan
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * 
 * @version $Revision: 480424 $
 * @since 1.0
 */
public class RangedGetMethod extends HttpRequestBase {

    // -------------------------------------------------------------- Constants
    /** Log object for this class. */
    private static final Logger log = LoggerFactory.getLogger(RangedGetMethod.class);

    public RangedGetMethod(String uri, List<Range> dataRanges) throws URISyntaxException {
        setURI(new URI(uri));
        if (dataRanges != null && !dataRanges.isEmpty()) {
            String rangeHeaderVal = getRangesRequest(dataRanges);
			log.info("ranges: " + rangeHeaderVal);
            setHeader("Range", "bytes=" + rangeHeaderVal);
        } else {
			log.info("No ranges to get");
		}
    }

    private String getRangesRequest(List<Range> ranges) {
        StringBuilder sb = new StringBuilder();
        for (Range d : ranges) {
            sb.append(d.getRange()).append(",");
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }

    @Override
    public String getMethod() {
        return "GET";
    }
}
