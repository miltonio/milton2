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

package io.milton.http.exceptions;

import io.milton.resource.Resource;

/**
 *
 * @author brad
 */
public class BadRequestException extends MiltonException {
    private static final long serialVersionUID = 1L;

    private final String reason;

    public BadRequestException(Resource r) {
        super(r);
        this.reason = null;
    }

    public BadRequestException(Resource r, String reason) {
        super(r);
        this.reason = reason;
    }
	
    public BadRequestException(String reason) {
        super();
        this.reason = reason;
    }	
	
    public BadRequestException(String reason, Throwable cause) {
        super(cause); 
        this.reason = reason;
    }		

    /**
     * Optional property, which describe the cause of the exception
     * @return
     */
    public String getReason() {
        return reason;
    }



}
