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

package io.milton.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Immutable
 */
public class Path implements Serializable {

    private static final long serialVersionUID = -8411900835514833454L;
    private final Path parent;
    private final String name;
    public static final Path root = new Path();
    private int hash;
    private final int length;
    public static final LengthComparator LENGTH_COMPARATOR = new LengthComparator();


    public static Path path( Path parent, String path ) {
        if( path == null )
            throw new NullPointerException( "The path parameter may not be null" );
        return split( parent, path );
    }

    public static Path path( String path ) {
        if( path == null || path.length() == 0 ) return root;
        return split( null, path );
    }

    private static Path split( Path startFrom, String s ) {
        Path parent = startFrom;
        StringBuilder sb = null;
        for( int i = 0; i < s.length(); i++ ) {
            char c = s.charAt( i );
            switch( c ) {
                case '/':
                    if( sb == null ) {
                        parent = root;
                    } else {
                        if( sb.length() > 0 ) {
                            String ss = sb.toString();
                            if( parent != null ) parent = parent.child( ss );
                            else parent = new Path( null, ss );
                        }
                        sb = null;
                    }
                    break;
//                case ' ':
//                    // ignore
//                    break;
                default:
                    if( sb == null ) sb = new StringBuilder();
                    sb.append( c );
            }
        }
        if( sb != null ) {
            if( sb.length() > 0 ) {
                String ss = sb.toString();
                if( parent != null ) parent = parent.child( ss );
                else parent = new Path( null, ss );
            }
        }

        return parent;
    }

    private Path() {
        this.parent = null;
        this.name = null;
        length = 0;
    }

    private Path( Path parent, String name ) {
        if( name == null )
            throw new IllegalArgumentException( "name may not be null" );
        this.parent = parent;
        this.name = name;
        if( this.parent != null ) {
            this.length = this.parent.length + 1;
        } else {
            this.length = 1;
        }
    }


    public int getLength() {
        return length;
    }

    public String[] getParts() {
        String[] arr = new String[length];
        Path p = this;
        int i = length;
        while( i > 0 ) {
            arr[--i] = p.getName();
            p = p.getParent();
        }
        return arr;
    }

    /**
     * 
     * @return - the first part of the path. ie a/b/c returns a
     */
    public String getFirst() {
        Path p = this;
        while( p.getParent() != null ) {
            Path next = p.getParent();
            if( next.getName() == null ) return p.getName();
            p = next;
        }
        if( p != null ) return p.getName();
        return null;
    }

    public List<String> getAfterFirst() {
        List<String> afterFirst = new ArrayList<String>();
        Path p = this;
        while( p != null && p.getParent() != null && !p.getParent().isRoot() ) {
            afterFirst.add( 0, p.getName() );
            p = p.getParent();
            if( p == null ) break;
        }
        return afterFirst;
    }

    public Path getStripFirst() {
        return stripFirst( this );
    }

    Path stripFirst( Path p ) {
        Path pParent = p.getParent();
        if( pParent == null || pParent.isRoot() ) return root;
        pParent = stripFirst( pParent );
        return new Path( pParent, p.getName() );
    }

    public String getName() {
        return name;
    }

    public Path getParent() {
        return parent;
    }

    public boolean isRoot() {
        return ( ( parent == null ) && ( name == null ) );
    }

    public String toPath() {
        if( isRoot() ) return "";
        if( parent == null ) return name;
        return parent.toString() + '/' + name;
    }

    @Override
    public String toString() {
        return toPath();
    }

    public String toString( String delimiter ) {
        if( parent == null ) return "";
        if( parent == null ) return name;
        return parent.toString( delimiter ) + delimiter + name;
    }

    public static Path root() {
        return root;
    }

    @Override
    public int hashCode() {
        if( hash == 0 ) {
            if( parent == null ) {
                hash = 158;
            } else {
                hash = parent.hashCode() ^ name.hashCode();
            }
        }
        return hash;
    }

    @Override
    public boolean equals( Object obj ) {
        if( obj == null ) return false;
        if( obj instanceof Path ) {
            Path p2 = (Path) obj;
            if( this.isRoot() ) {
                return p2.isRoot();
            } else {
                if( parentEquals( this, p2 ) ) {
                    return this.name.equals( p2.name );
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private static boolean parentEquals( Path p1, Path p2 ) {
        if( p2.parent == null ) {
            return p1.parent == null;
        } else {
            return p2.parent.equals( p1.parent );
        }
    }

    public Path child( String name ) {
        Path ch = new Path( this, name );
        return ch;
    }

    public boolean isRelative() {
        if( parent == null ) {
            return !isRoot();
        } else {
            return parent.isRelative();
        }
    }

    /**
     * Add the path components of the given path to this one.
     *
     * Eg "a/b/c" + "/d/e/f" = "a/b/c/d/e/f"
     *
     * @param p
     * @return
     */
    public Path add(Path p) {
        Path x = this;
        for(String s : p.getParts()) {
            x = x.child(s);
        }
        return x;
    }

    public static class LengthComparator implements Comparator<Path> {

        public int compare( Path o1, Path o2 ) {
            Integer i1 = o1.getLength();
            Integer i2 = o2.getLength();
            return i1.compareTo( i2 );
        }
    }
}
