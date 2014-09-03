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
package io.milton.mail;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Passes the request and response along a series of filters
 *
 *  By default the MailServer loads a single filter which executes the appropriate
 *  handler for the http method
 *
 *  Additional filters can be added using HttpManager.addFilter
 */
public class FilterChain {

    private final static Logger log = LoggerFactory.getLogger(FilterChain.class);

    final List<Filter> filters;
    final Filter terminal;
    int pos = 0;

    public FilterChain(List<Filter> filters, Filter terminal) {
        this.filters =  filters;
        this.terminal = terminal;

    }

    public void doEvent(Event event) {
        if( pos < filters.size() ) {
            Filter filter = filters.get(pos++);
            if( filter != null ) {
                filter.doEvent(this,event);
                return ;
            }
            log.warn( "Configuration problem. null filter at position: " + pos);
        }
        if( terminal != null ) {
            terminal.doEvent(this, event);
        } else {
            log.warn("there appears to be no filters to process the request! Should be at least a terminal filter");
        }
    }
}
