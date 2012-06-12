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

import io.milton.resource.Resource;
/**
 * Represents an address resource. 
 * 
 * Example(1):
 * <C:address-data>.......................</C:address-data>
 * 
 * 
 * Example(2): (Not Supported yet)
 * <C:address-data>
 *   <C:prop name="VERSION"/>
 *   <C:prop name="UID"/>
 *   <C:prop name="NICKNAME"/>
 *   <C:prop name="EMAIL"/>
 *   <C:prop name="FN"/>
 * </C:address-data>
 * 
 * @author nabil.shams
 */
public interface AddressResource extends Resource{
    String getAddressData();
}
