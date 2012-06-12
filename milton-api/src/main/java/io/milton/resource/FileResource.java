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

package io.milton.resource;

import io.milton.resource.PropFindableResource;
import io.milton.resource.PostableResource;
import io.milton.resource.GetableResource;

/** 
 * Extends all interfaces required for typical document behavior.
 * <P/>
 * This is a good place to start if you want a normal resource. However, think
 * carefully about which interfaces to implement. Only implement those which
 * should actually be supported
 */
public interface FileResource extends CopyableResource, DeletableResource, GetableResource, MoveableResource, PostableResource, PropFindableResource {
    
}
