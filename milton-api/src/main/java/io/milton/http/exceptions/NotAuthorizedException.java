/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.milton.http.exceptions;

import io.milton.resource.Resource;

/**
 *  Indicates that the current user is not able to perform the requested operation
 *
 * This should not normally be used. Instead, a resource should determine if
 * a user can perform an operation in its authorised method
 *
 * However, this exception allows for cases where the authorised status can
 * only be determined during processing
 */
public class NotAuthorizedException extends MiltonException{
    private static final long serialVersionUID = 1L;

    public NotAuthorizedException() {
    }    
    
    public NotAuthorizedException(Resource r) {
        super(r);
    }

    public NotAuthorizedException(Resource r, Throwable cause) {
        super(r, cause);
    }    
    
	public NotAuthorizedException(String message, Resource r) {
		super(message, r);
	}
	
	
	

}
