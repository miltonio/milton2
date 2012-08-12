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
package io.milton.http;

import io.milton.resource.AccessControlledResource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author brad
 */
public class AclUtils {
    
    /**
     * Recurisve function which checks the given collection of priviledges, 
     * and checks inside the contains property of those priviledges
     * 
     * Returns true if the required priviledge is directly present in the collection
     * or is implied
     * 
     * @param required
     * @param privs
     * @return 
     */
    public static boolean containsPriviledge(AccessControlledResource.Priviledge required, Iterable<AccessControlledResource.Priviledge> privs) {
        for (AccessControlledResource.Priviledge p : privs) {
            if (p.equals(required)) {
                return true;
            }
            if( containsPriviledge(required, p.contains)) {
                return true;
            }
        }
        return false;
    }      
    
    public static Set<AccessControlledResource.Priviledge> asSet(AccessControlledResource.Priviledge ... privs) {
        Set<AccessControlledResource.Priviledge> set = new HashSet<AccessControlledResource.Priviledge>(privs.length);
        set.addAll(Arrays.asList(privs));
        return set;
    }
}
