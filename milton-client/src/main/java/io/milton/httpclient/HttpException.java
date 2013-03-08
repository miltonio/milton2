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

package io.milton.httpclient;

/**
 *
 * @author mcevoyb
 */
public abstract class HttpException extends Exception {

    private static final long serialVersionUID = 1L;
    private final int result;
    private final String href;

    public HttpException(int result, String href) {
        super("http error: " + result + " - " + href);
        this.result = result;
        this.href = href;
    }

    public HttpException(String href, Throwable cause) {
        super(href, cause);
        this.href = href;
        this.result = 0;
    }
    
    

    public int getResult() {
        return result;
    }

    public String getHref() {
        return href;
    }
}
