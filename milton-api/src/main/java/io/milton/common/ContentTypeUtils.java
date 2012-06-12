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

package io.milton.common;

import eu.medsea.mimeutil.MimeException;
import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import java.io.File;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class ContentTypeUtils {

    private static Logger log = LoggerFactory.getLogger(ContentTypeUtils.class);

    static {
        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
    }

    public static String findContentTypes( String name ) {
        Collection mimeTypes = MimeUtil.getMimeTypes( name );
        return mimeTypes.toString();
        //return buildContentTypeText(mimeTypes);
    }

    public static String findContentTypes( File file ) {
        Collection mimeTypes = null;
        try {
            mimeTypes = MimeUtil.getMimeTypes( file.getName() );
        } catch( MimeException e ) {
            log.warn( "exception retrieving content type for file: " + file.getAbsolutePath(),e);
            return "application/binary";
        }
        String s = mimeTypes.toString();
        //String s = buildContentTypeText(mimeTypes);
        log.trace( "findContentTypes: {}", file.getName(), mimeTypes);
        return s;
    }

    public static String findAcceptableContentType(String mime, String preferredList) {
        MimeType mt = MimeUtil.getPreferedMimeType(preferredList, mime);
        return mt.toString();

    }

    private static String buildContentTypeText( Collection mimeTypes ) {
        StringBuilder sb = null;
        for( Object o : mimeTypes ) {
            MimeType mt = (MimeType) o;
            if( sb == null ) {
                sb = new StringBuilder();
            } else {
                sb.append( "," );
            }
            sb.append( mt.toString() );
        }
        if( sb == null ) {
            return "";
        } else {
            return sb.toString();
        }
    }
}
