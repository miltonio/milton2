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

package io.milton.resource;

/**
 * Extends all interfaces required for typical folder behavior.
 * <P/>
 * This is a good place to start if you want a normal directory. However, think
 * carefully about which interfaces to implement. Only implement those which
 * should actually be supported. Eg, only implement MoveableResource if it can be moved
 */
public interface FolderResource extends MakeCollectionableResource, PutableResource, CopyableResource, DeletableResource, GetableResource, MoveableResource, PropFindableResource{
    
}
