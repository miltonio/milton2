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
