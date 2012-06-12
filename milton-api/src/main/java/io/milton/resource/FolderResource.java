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

/**
 * Extends all interfaces required for typical folder behavior.
 * <P/>
 * This is a good place to start if you want a normal directory. However, think
 * carefully about which interfaces to implement. Only implement those which
 * should actually be supported. Eg, only implement MoveableResource if it can be moved
 */
public interface FolderResource extends MakeCollectionableResource, PutableResource, CopyableResource, DeletableResource, GetableResource, MoveableResource, PropFindableResource{
    
}
