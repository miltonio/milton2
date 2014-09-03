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

package io.milton.principal;

import io.milton.http.values.HrefList;
import io.milton.principal.DiscretePrincipal;

/**
 *
 * @author brad
 */
public interface CardDavPrincipal extends DiscretePrincipal {
    /**
     * This is usually a single href which identifies the collection which
     * contains the users addressbooks. This might be the user's own href.
     *
     */
    HrefList getAddressBookHomeSet();
    
    /**
     * Returns the URL of an address object resource that corresponds to the 
     * user represented by the principal.
     * 
     */
    String getAddress();

}
