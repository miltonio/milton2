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

package io.milton.hashsplit;

import java.util.List;

/**
 * This provides a data structure which is a tree of long values. This
 * can be used to represent the "fanout" structure described in the Bup DESIGN
 * page
 * 
 * https://github.com/apenwarr/bup/blob/master/DESIGN
 *
 * @author brad
 */
public class HashNode {
    private long hashValue;
    private List<HashNode> childNodes;

    public List<HashNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(List<HashNode> childNodes) {
        this.childNodes = childNodes;
    }

    public long getHashValue() {
        return hashValue;
    }

    public void setHashValue(long hashValue) {
        this.hashValue = hashValue;
    }
    
    
    
}
