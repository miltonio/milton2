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

package io.milton.http.quota;

import io.milton.common.Path;
import io.milton.resource.CollectionResource;
import io.milton.resource.GetableResource;
import io.milton.resource.QuotaResource;
import io.milton.http.Request;
import io.milton.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default storage checking mechanism checks to see if the nearest parent
 * resource implements QuotaResource, and if so uses the available bytes
 * property from that to determine whether to allow a PUT
 *
 * Note that there is not always information available giving the size of new content,
 * and in the case of replacing an existing resource, we might not have that resource's
 * size. In these cases we cant determine perfectly whether we should allow or reject
 * a PUT request. So we allow these requests if the available storage is greater then
 * zero, but disallow if less then or equal to zero.
 *
 * @author brad
 */
public class DefaultStorageChecker implements StorageChecker {

    private final static Logger log = LoggerFactory.getLogger( DefaultStorageChecker.class );

    public StorageErrorReason checkStorageOnReplace( Request request, CollectionResource parent, Resource replaced, String host ) {
        if( parent instanceof QuotaResource ) {
            QuotaResource qr = (QuotaResource) parent;
            Long llAvail = qr.getQuotaAvailable();
            if( llAvail == null ) {
                log.debug( "no quota data available" );
                return null;
            }
            if( llAvail <= 0 ) {
                // new content length must be less then existing
                Long newContentLength = request.getContentLengthHeader();
                if( newContentLength == null ) {
                    log.debug( "new content length is not available, cant check quota, reject" );
                    return StorageErrorReason.SER_QUOTA_EXCEEDED;
                }
                if( replaced instanceof GetableResource ) {
                    GetableResource gr = (GetableResource) replaced;
                    Long existingLength = gr.getContentLength();
                    if( existingLength == null ) {
                        log.debug( "existing content length cant be determined, cant check quota, reject");
                        return StorageErrorReason.SER_QUOTA_EXCEEDED;
                    } else {
                        long diff = existingLength - newContentLength;
                        if( diff > 0 ) {
                            return null;
                        } else {
                            log.debug( "new content is larger then existing content, but no quota is available, reject");
                            return StorageErrorReason.SER_QUOTA_EXCEEDED;
                        }
                    }
                } else {
                    log.debug( "existing content length cant be determined, cant check quota, reject");
                    return StorageErrorReason.SER_QUOTA_EXCEEDED;
                }
            } else {
                // difference of new content to existing must be less then available, but if in doubt allow
                Long newContentLength = request.getContentLengthHeader();
                if( newContentLength == null ) {
                    log.debug( "new content length is not available, cant check quota, allow" );
                    return null;
                }
                if( replaced instanceof GetableResource ) {
                    GetableResource gr = (GetableResource) replaced;
                    Long existingLength = gr.getContentLength();
                    if( existingLength == null ) {
                        log.debug( "existing content length cant be determined, cant check quota, allow");
                        return null;
                    } else {
                        long diff = newContentLength - existingLength; // this is the amount extra needed
                        if( diff <= llAvail ) {
                            return null;
                        } else {
                            log.debug( "new content is larger then existing content, but no quota is available, reject");
                            return StorageErrorReason.SER_QUOTA_EXCEEDED;
                        }
                    }
                } else {
                    log.debug( "existing content length cant be determined, cant check quota, allow");
                    return null;
                }
            }
            // if difference between new content and existing is less then available, then ok


        } else {
            return null;
        }
    }

    public StorageErrorReason checkStorageOnAdd( Request request, CollectionResource nearestParent, Path parentPath, String host ) {
        if( nearestParent instanceof QuotaResource ) {
            QuotaResource qr = (QuotaResource) nearestParent;
            Long llAvail = qr.getQuotaAvailable();
            if( llAvail == null ) {
                log.debug( "no quota data available" );
                return null;
            }
            if( llAvail <= 0 ) {
                log.debug( "no quota available, reject" );
                return StorageErrorReason.SER_QUOTA_EXCEEDED;
            } else {
                // new content must be less then that available
                Long newContentLength = request.getContentLengthHeader();
                if( newContentLength == null ) {
                    log.debug( "new content length is not available, cant check quota, allow" );
                    return null;
                }
                if( newContentLength < llAvail ) {
                    return null;
                } else {
                    log.debug( "new content length is greater then available storage, reject");
                    return StorageErrorReason.SER_QUOTA_EXCEEDED;
                }
            }
        } else {
            return null;
        }

    }
}
