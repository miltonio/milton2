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

package io.milton.http;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * 
 * 
 * This is just initial experimental design for solution to already compressed resources problem
 *
 * @author brad
 */
public interface CompressedResource {
	
	/**
	 * Return the supported encoding from the list of allowable encodings from the user agent
	 * 
	 * If none are supported return null.
	 * 
	 * @param acceptableEncodings
	 * @return - null if none of the given encodings are supported, otherwise the content encoding header value to be used
	 */
	String getSupportedEncoding(String acceptableEncodings);
	
	/**
	 * 
	 * @param contentEncoding - the supported encoding returned from getSupportedEncoding
	 * @param out
	 * @param range
	 * @param params
	 * @param contentType
	 * @throws IOException
	 * @throws NotAuthorizedException
	 * @throws BadRequestException
	 * @throws NotFoundException 
	 */
	void sendCompressedContent( String contentEncoding, OutputStream out, Range range, Map<String,String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException;
	
	/**
	 * Return the content length, if known, for the given encoding. Otherwise 
	 * return null
	 * 
	 * @param contentEncoding
	 * @return - null, or the length of the content encoded with the given encoding
	 */
	Long getCompressedContentLength(String contentEncoding);
}
