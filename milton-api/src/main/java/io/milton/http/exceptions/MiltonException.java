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
 *  Base class for exceptions during processing requests on resources
 */
public abstract class MiltonException extends Exception {
    private static final long serialVersionUID = 1L;
    private Resource resource;

	public MiltonException() {
	}
    public MiltonException(String message, Resource resource) {
		super(message);
        this.resource = resource;
    }

    public MiltonException(Resource resource, Throwable cause) {
        super(cause);
        this.resource = resource;
    }
    
    
    public MiltonException(Resource resource) {
        this.resource = resource;
    }
    
	public MiltonException(String message) {
		super(message);
	}
	
	public MiltonException(Throwable cause) {
		super(cause);
	}	

    public Resource getResource() {
        return resource;
    }
    
}
