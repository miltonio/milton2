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

/* ChainingHash.java

   ChainingHash: Chaining hash table for storing checksums from metafile
   Copyright (C) 2011 Tomáš Hlavnička <hlavntom@fel.cvut.cz>

   This file is a part of Jazsync.

   Jazsync is free software; you can redistribute it and/or modify it
   under the terms of the GNU General Public License as published by the
   Free Software Foundation; either version 2 of the License, or (at
   your option) any later version.

   Jazsync is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Jazsync; if not, write to the

      Free Software Foundation, Inc.,
      59 Temple Place, Suite 330,
      Boston, MA  02111-1307
      USA
 */

package io.milton.zsync;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Chaining hash table used to store block checksums loaded from metafile
 * @author Tomáš Hlavnička
 */
public class ChainingHash {
    private ArrayList<ArrayList> hashArray;
    private int arraySize;
    private int index;

    /**
     * Initializing chaining hash table of <code>size</code>
     * @param size Size of the hash table
     */
    public ChainingHash(int size){
        arraySize=size;
        hashArray = new ArrayList<ArrayList>(arraySize);
        for (int i = 0; i < arraySize; i++) {
            hashArray.add(i, new ArrayList<ChecksumPair>());
        }
    }

    /**
     * Hashing function
     * @param pKey Key object that will be hashed into the table
     * @return Index in hash table where the object will be put
     */
    public int hashFunction(ChecksumPair pKey) {
        return pKey.hashCode() % arraySize;
    }

    /**
     * Method inserting object into the table
     * @param pKey Object we are inserting
     */
    public void insert(ChecksumPair pKey){
        int hashValue = hashFunction(pKey);
        hashArray.get(hashValue).add(pKey);
    }

    /**
     * Method used to delete an object from hash table
     * @param pKey Object we want to delete from table
     */
    public void delete(ChecksumPair pKey){
        int hashValue = hashFunction(pKey);
        hashArray.get(hashValue).remove(pKey);
    }

    /**
     * Method used to find an object in hash table using only weakSum
     * @param pKey Object that we are looking for
     * @return Object if found, null if not
     */
    public ChecksumPair find(ChecksumPair pKey){
        int hashValue = hashFunction(pKey);
        ChecksumPair p = null;
        for(int i=0;i<hashArray.get(hashValue).size();i++){
            p = (ChecksumPair) hashArray.get(hashValue).get(i);
            if(p.getWeak()==pKey.getWeak()){
                index = i;
                return p;
            }
        }
        return null;
    }

    /**
     * Method used to find an object in hash table using weakSum and strongSum
     * @param pKey Object that we are looking for
     * @return Object if found, null if not
     */
    public ChecksumPair findMatch(ChecksumPair pKey){
        int hashValue = hashFunction(pKey);
        ChecksumPair p = (ChecksumPair) hashArray.get(hashValue).get(index);
        if(p.getWeak()==pKey.getWeak() && Arrays.equals(p.getStrong(), pKey.getStrong())){
            return p;
        } else {
            p = null;
        }
        for(int i=0;i<hashArray.get(hashValue).size();i++){
            p = (ChecksumPair) hashArray.get(hashValue).get(i);
            if(p.getWeak()==pKey.getWeak() && Arrays.equals(p.getStrong(), pKey.getStrong())){
                return p;
            }
        }
        return null;
    }

    /**
     * Simple method used to write out the content of hash table
     */
    public void displayTable(){
        for(int l=0;l<hashArray.size();l++){
            for(int i = 0;i<hashArray.get(l).size();i++){
                System.out.println(l+". list: " +((ChecksumPair) (hashArray.get(l).get(i))).toString());
            }
        }
    } 
}
